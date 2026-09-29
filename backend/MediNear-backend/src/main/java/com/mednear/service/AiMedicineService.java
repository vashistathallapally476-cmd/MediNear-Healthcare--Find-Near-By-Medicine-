package com.mednear.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mednear.dto.request.AiChatRequest;
import com.mednear.dto.response.AiChatResponse;
import com.mednear.dto.response.NearbyMedicineResponse;
import com.mednear.dto.response.PagedResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

/**
 * AI Medicine Assistant — powered by Claude (Anthropic).
 *
 * Flow:
 *   1. User sends a message like "I need fever medicine near me"
 *   2. We send it to Claude with a system prompt that describes MediNear
 *      and asks Claude to:
 *      a) Identify the intent (SEARCH / INFO / GENERAL)
 *      b) Extract medicine names the user needs
 *      c) Give a helpful natural language reply
 *   3. We parse Claude's structured JSON response
 *   4. If intent is SEARCH and user provided lat/lng:
 *      → We call MedicineService.searchNearby() with each extracted medicine
 *      → We attach the real pharmacy results to the AI response
 *   5. Return { reply, suggestedMedicines, nearbyStores, intent } to frontend
 *
 * The frontend shows:
 *   - Claude's reply as a chat bubble
 *   - Pharmacy cards below (like Zomato showing restaurants after AI suggestion)
 */
@Service
public class AiMedicineService {

    private final WebClient    webClient;
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private MedicineService medicineService;

    @Value("${anthropic.api-key}")
    private String apiKey;

    @Value("${anthropic.model:claude-sonnet-4-20250514}")
    private String model;

    @Value("${anthropic.max-tokens:1024}")
    private int maxTokens;

    public AiMedicineService(@Value("${anthropic.api-url}") String apiUrl) {
        this.webClient = WebClient.builder()
            .baseUrl(apiUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader("anthropic-version", "2023-06-01")
            .build();
    }

    public AiChatResponse chat(AiChatRequest req) {

        // ── 1. Build the prompt ──────────────────────────────────
        String systemPrompt = buildSystemPrompt(req.getLatitude(), req.getLongitude());
        String userMessage  = req.getMessage();

        // ── 2. Call Claude API ───────────────────────────────────
        String claudeReply = callClaude(systemPrompt, userMessage);

        // ── 3. Parse Claude's structured JSON response ───────────
        AiChatResponse response = parseClaudeResponse(claudeReply, req.getMessage());

        // ── 4. If SEARCH intent + location → fetch real pharmacies ──
        if ("SEARCH".equals(response.getIntent())
                && req.getLatitude() != null
                && req.getLongitude() != null
                && response.getSuggestedMedicines() != null
                && !response.getSuggestedMedicines().isEmpty()) {

            List<NearbyMedicineResponse> allStores = new ArrayList<>();
            for (String medName : response.getSuggestedMedicines()) {
                try {
                    PagedResponse<NearbyMedicineResponse> results =
                        medicineService.searchNearby(
                            medName,
                            req.getLatitude(),
                            req.getLongitude(),
                            5.0, 0, 5   // 5km radius, first page, max 5 results per medicine
                        );
                    allStores.addAll(results.getContent());
                } catch (Exception ignored) {
                    // If a specific medicine search fails, continue with others
                }
            }
            response.setNearbyStores(allStores);
        }

        return response;
    }

    // ── Private: Build system prompt ────────────────────────────

    private String buildSystemPrompt(Double lat, Double lng) {
        String locationContext = (lat != null && lng != null)
            ? "The user is located at latitude " + lat + ", longitude " + lng + ". "
              + "They want to find nearby pharmacies."
            : "The user has not shared their location.";

        return """
            You are MediNear AI — a helpful medical assistant for the MediNear platform.
            MediNear is an app that helps users find medicines at nearby pharmacies,
            similar to how Swiggy/Zomato shows nearby restaurants.

            """ + locationContext + """

            Your job is to:
            1. Understand what medicines the user needs
            2. Provide helpful, accurate medical information
            3. Suggest appropriate medicines for their symptoms if asked
            4. Always recommend consulting a doctor for serious conditions

            IMPORTANT: You must ALWAYS respond with valid JSON in this exact format:
            {
              "intent": "SEARCH" | "INFO" | "GENERAL",
              "reply": "Your helpful natural language response here",
              "suggestedMedicines": ["MEDICINE1", "MEDICINE2"]
            }

            Intent definitions:
            - SEARCH: User wants to FIND/BUY a medicine (e.g. "where can I get paracetamol", "I need fever medicine")
            - INFO: User wants INFORMATION about a medicine (e.g. "what is paracetamol used for", "side effects of ibuprofen")
            - GENERAL: General health question not related to finding a specific medicine

            For suggestedMedicines:
            - Include medicine names in UPPERCASE
            - Only include medicines relevant to the user's query
            - Use common/generic names (e.g. PARACETAMOL, not brand names)
            - Maximum 3 medicines

            Examples of suggestedMedicines:
            - "I have fever" → ["PARACETAMOL 500MG", "IBUPROFEN 400MG"]
            - "I have a cold and cough" → ["CETIRIZINE 10MG", "DEXTROMETHORPHAN"]
            - "stomach pain" → ["OMEPRAZOLE 20MG", "PANTOPRAZOLE 40MG"]

            Always be helpful, accurate, and recommend seeing a doctor for serious symptoms.
            Respond ONLY with the JSON object — no text before or after it.
            """;
    }

    // ── Private: Call Claude API ─────────────────────────────────

    private String callClaude(String systemPrompt, String userMessage) {
        try {
            ObjectNode body = mapper.createObjectNode();
            body.put("model",      model);
            body.put("max_tokens", maxTokens);
            body.put("system",     systemPrompt);

            ArrayNode messages = body.putArray("messages");
            ObjectNode msg = messages.addObject();
            msg.put("role",    "user");
            msg.put("content", userMessage);

            JsonNode response = webClient.post()
                .uri("/v1/messages")
                .header("x-api-key", apiKey)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

            if (response == null) return fallbackResponse("API returned null");

            return response.path("content").get(0).path("text").asText();

        } catch (Exception e) {
            return fallbackResponse(e.getMessage());
        }
    }

    // ── Private: Parse Claude's JSON reply ──────────────────────

    private AiChatResponse parseClaudeResponse(String claudeText, String originalMessage) {
        AiChatResponse result = new AiChatResponse();
        try {
            // Strip potential markdown code blocks if Claude wrapped it
            String clean = claudeText
                .replaceAll("```json\\s*", "")
                .replaceAll("```\\s*",     "")
                .trim();

            JsonNode json = mapper.readTree(clean);

            result.setIntent(json.path("intent").asText("GENERAL"));
            result.setReply(json.path("reply").asText(claudeText));

            List<String> meds = new ArrayList<>();
            for (JsonNode med : json.path("suggestedMedicines")) {
                meds.add(med.asText());
            }
            result.setSuggestedMedicines(meds);

        } catch (Exception e) {
            // Claude didn't return valid JSON — use raw text as reply
            result.setIntent("GENERAL");
            result.setReply(claudeText);
            result.setSuggestedMedicines(List.of());
        }
        return result;
    }

    private String fallbackResponse(String reason) {
        return """
            {
              "intent": "GENERAL",
              "reply": "I'm having trouble connecting right now. Please try again in a moment.",
              "suggestedMedicines": []
            }
            """;
    }
}
