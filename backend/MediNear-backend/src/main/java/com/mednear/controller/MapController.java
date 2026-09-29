package com.mednear.controller;

import com.mednear.dto.response.ApiResponse;
import com.mednear.dto.response.MapRouteResponse;
import com.mednear.service.GoogleMapsService;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Map & Navigation endpoints.
 *
 * GET /api/maps/route
 *   → Returns driving/walking route from user to a pharmacy
 *   → Includes encoded polyline, steps, ETA, distance, deep-link
 *
 * GET /api/maps/static-url
 *   → Returns a Google Static Maps image URL showing the pharmacy pin
 *   → Frontend can use this as an <img src="..."> preview thumbnail
 *
 * Both endpoints are PUBLIC — guests can navigate to a pharmacy
 * without needing a login.
 */
@RestController
@RequestMapping("/api/maps")
@Validated
public class MapController {

    @Autowired
    private GoogleMapsService googleMapsService;

    /**
     * GET /api/maps/route
     *   ?originLat=17.385
     *   &originLng=78.486
     *   &destLat=17.390
     *   &destLng=78.491
     *   &mode=driving          (optional: driving | walking | bicycling | transit)
     *
     * Returns full route info including polyline for the map,
     * turn-by-turn steps, distance, ETA, and "Open in Google Maps" URL.
     */
    @GetMapping("/route")
    public ResponseEntity<ApiResponse<MapRouteResponse>> getRoute(
            @RequestParam @DecimalMin("-90.0")  @DecimalMax("90.0")  double originLat,
            @RequestParam @DecimalMin("-180.0") @DecimalMax("180.0") double originLng,
            @RequestParam @DecimalMin("-90.0")  @DecimalMax("90.0")  double destLat,
            @RequestParam @DecimalMin("-180.0") @DecimalMax("180.0") double destLng,
            @RequestParam(defaultValue = "driving") String mode) {

        MapRouteResponse data = googleMapsService.getRoute(
            originLat, originLng, destLat, destLng, mode);

        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    /**
     * GET /api/maps/static-url
     *   ?lat=17.390&lng=78.491&label=Apollo+Pharmacy
     *
     * Returns a ready-to-use Google Static Maps image URL.
     * Frontend uses this as the map thumbnail on the pharmacy card.
     *
     * Example output:
     * "https://maps.googleapis.com/maps/api/staticmap?center=17.39,78.491
     *  &markers=color:red|label:A|17.39,78.491&size=400x200&key=..."
     */
    @GetMapping("/static-url")
    public ResponseEntity<ApiResponse<String>> staticMapUrl(
            @RequestParam @DecimalMin("-90.0")  @DecimalMax("90.0")  double lat,
            @RequestParam @DecimalMin("-180.0") @DecimalMax("180.0") double lng,
            @RequestParam(defaultValue = "Pharmacy") String label) {

        String url = "https://maps.googleapis.com/maps/api/staticmap"
            + "?center=" + lat + "," + lng
            + "&zoom=15"
            + "&size=400x200"
            + "&markers=color:red%7Clabel:P%7C" + lat + "," + lng
            + "&key=" + googleMapsService.getApiKey();

        return ResponseEntity.ok(ApiResponse.ok(url));
    }
}
