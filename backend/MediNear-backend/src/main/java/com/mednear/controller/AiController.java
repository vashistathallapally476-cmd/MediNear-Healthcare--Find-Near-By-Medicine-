package com.mednear.controller;

import com.mednear.dto.request.AiChatRequest;
import com.mednear.dto.response.AiChatResponse;
import com.mednear.dto.response.ApiResponse;
import com.mednear.service.AiMedicineService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AI Medicine Assistant endpoints.
 *
 * POST /api/ai/chat
 *   → Natural language conversation with Claude
 *   → Auto-fetches nearby pharmacies if location is provided
 *
 * Both endpoints are PUBLIC — no JWT required for AI chat,
 * so users can use the assistant before signing up.
 */
@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private AiMedicineService aiMedicineService;

    /**
     * POST /api/ai/chat
     *
     * Request body:
     * {
     *   "message": "I have fever and headache, what medicine do I need?",
     *   "latitude": 17.385,       // optional — enables pharmacy search
     *   "longitude": 78.486
     * }
     *
     * Response:
     * {
     *   "success": true,
     *   "data": {
     *     "reply": "For fever and headache, Paracetamol 500mg is commonly used...",
     *     "intent": "SEARCH",
     *     "suggestedMedicines": ["PARACETAMOL 500MG", "IBUPROFEN 400MG"],
     *     "nearbyStores": [ ... pharmacy cards with distance ... ]
     *   }
     * }
     */
    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<AiChatResponse>> chat(
            @Valid @RequestBody AiChatRequest req) {
        AiChatResponse data = aiMedicineService.chat(req);
        return ResponseEntity.ok(ApiResponse.ok(data));
    }
}
