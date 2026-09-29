package com.mednear.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.mednear.dto.response.MapRouteResponse;
import com.mednear.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Google Maps Directions API integration.
 *
 * Endpoint used: GET https://maps.googleapis.com/maps/api/directions/json
 *
 * What it provides to MediNear:
 *   1. Driving/walking route from user → pharmacy
 *   2. Encoded polyline   → frontend draws the route line on the map
 *   3. Turn-by-turn steps → shown in bottom sheet like Swiggy/Zomato
 *   4. Distance + ETA     → shown on the pharmacy card
 *   5. Deep-link URL      → "Open in Google Maps" button
 *
 * Future extension: swap Directions for Routes API (newer, supports
 * traffic-aware ETA and alternative routes).
 */
@Service
public class GoogleMapsService {

    private final WebClient webClient;

    @Value("${google.maps.api-key}")
    private String apiKey;

    public GoogleMapsService(@Value("${google.maps.base-url}") String baseUrl) {
        this.webClient = WebClient.builder()
            .baseUrl(baseUrl)
            .build();
    }

    /**
     * Get driving route from user location → pharmacy.
     *
     * @param originLat      user's latitude
     * @param originLng      user's longitude
     * @param destinationLat pharmacy latitude
     * @param destinationLng pharmacy longitude
     * @param mode           "driving" | "walking" | "bicycling" | "transit"
     */
    public MapRouteResponse getRoute(double originLat, double originLng,
                                     double destinationLat, double destinationLng,
                                     String mode) {
        String travelMode = (mode == null || mode.isBlank()) ? "driving" : mode;

        String origin      = originLat      + "," + originLng;
        String destination = destinationLat + "," + destinationLng;

        // Call Google Directions API (blocking — fine for REST endpoint)
        JsonNode response = webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/maps/api/directions/json")
                .queryParam("origin",      origin)
                .queryParam("destination", destination)
                .queryParam("mode",        travelMode)
                .queryParam("key",         apiKey)
                .build())
            .retrieve()
            .bodyToMono(JsonNode.class)
            .block();

        // ── Validate Google API response ─────────────────────────
        if (response == null) {
            throw new BusinessException("No response from Google Maps API");
        }

        String status = response.path("status").asText();
        if (!"OK".equals(status)) {
            String errorMsg = response.path("error_message").asText("No route found");
            throw new BusinessException("Google Maps API error [" + status + "]: " + errorMsg);
        }

        // ── Parse the first route ────────────────────────────────
        JsonNode route = response.path("routes").get(0);
        JsonNode leg   = route.path("legs").get(0);

        MapRouteResponse result = new MapRouteResponse();

        // Polyline (for drawing route on map)
        result.setEncodedPolyline(
            route.path("overview_polyline").path("points").asText());

        // Distance + Duration
        result.setDistanceText(leg.path("distance").path("text").asText());
        result.setDistanceMeters(leg.path("distance").path("value").asInt());
        result.setDurationText(leg.path("duration").path("text").asText());
        result.setDurationSeconds(leg.path("duration").path("value").asInt());
        result.setStartAddress(leg.path("start_address").asText());
        result.setEndAddress(leg.path("end_address").asText());

        // Turn-by-turn steps
        List<MapRouteResponse.Step> steps = new ArrayList<>();
        for (JsonNode stepNode : leg.path("steps")) {
            MapRouteResponse.Step step = new MapRouteResponse.Step();
            // Strip HTML tags from instruction (Google returns <b>Turn left</b>)
            step.setInstruction(
                stepNode.path("html_instructions").asText()
                        .replaceAll("<[^>]*>", " ")
                        .replaceAll("\\s+", " ")
                        .trim());
            step.setDistance(stepNode.path("distance").path("text").asText());
            step.setDuration(stepNode.path("duration").path("text").asText());
            step.setStartLat(stepNode.path("start_location").path("lat").asDouble());
            step.setStartLng(stepNode.path("start_location").path("lng").asDouble());
            step.setEndLat(stepNode.path("end_location").path("lat").asDouble());
            step.setEndLng(stepNode.path("end_location").path("lng").asDouble());
            steps.add(step);
        }
        result.setSteps(steps);

        // Deep-link so user can tap "Open in Google Maps"
        result.setMapsUrl(buildMapsUrl(origin, destination, travelMode));

        return result;
    }

    /**
     * Geocode an address string → lat/lng.
     * Useful for stores that were registered by address only.
     */
    public double[] geocodeAddress(String address) {
        JsonNode response = webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/maps/api/geocode/json")
                .queryParam("address", address)
                .queryParam("key",     apiKey)
                .build())
            .retrieve()
            .bodyToMono(JsonNode.class)
            .block();

        if (response == null || !"OK".equals(response.path("status").asText())) {
            throw new BusinessException("Could not geocode address: " + address);
        }

        JsonNode location = response.path("results").get(0)
            .path("geometry").path("location");

        return new double[]{
            location.path("lat").asDouble(),
            location.path("lng").asDouble()
        };
    }

    // ── Private helpers ─────────────────────────────────────────

    private String buildMapsUrl(String origin, String destination, String mode) {
        return "https://www.google.com/maps/dir/?api=1"
            + "&origin="      + encode(origin)
            + "&destination=" + encode(destination)
            + "&travelmode="  + mode;
    }

    public String getApiKey() { return apiKey; }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
