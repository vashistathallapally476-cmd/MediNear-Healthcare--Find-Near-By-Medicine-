package com.mednear.controller;

import com.mednear.dto.response.ApiResponse;
import com.mednear.dto.response.NearbyMedicineResponse;
import com.mednear.dto.response.PagedResponse;
import com.mednear.service.MedicineService;
import jakarta.validation.constraints.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Medicine search and autocomplete endpoints.
 * Both are public — no JWT required.
 */
@RestController
@RequestMapping("/api/medicines")
@Validated
public class MedicineController {

    @Autowired private MedicineService medicineService;

    /**
     * GET /api/medicines/nearby
     *   ?medicine=Paracetamol
     *   &latitude=17.385
     *   &longitude=78.487
     *   &radius=5.0          (optional, default 5km)
     *   &page=0              (optional, default 0)
     *   &size=10             (optional, default 10)
     *
     * Returns stores within :radius km that have the medicine in stock,
     * sorted by distance ascending.
     */
    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<PagedResponse<NearbyMedicineResponse>>> searchNearby(
            @RequestParam @NotBlank String medicine,
            @RequestParam @DecimalMin("-90.0")  @DecimalMax("90.0")  double latitude,
            @RequestParam @DecimalMin("-180.0") @DecimalMax("180.0") double longitude,
            @RequestParam(defaultValue = "5.0") @Positive double radius,
            @RequestParam(defaultValue = "0")  @Min(0)   int page,
            @RequestParam(defaultValue = "10") @Min(1)   int size) {

        PagedResponse<NearbyMedicineResponse> data =
            medicineService.searchNearby(medicine, latitude, longitude, radius, page, size);

        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    /**
     * GET /api/medicines/autocomplete?q=para
     * Returns up to 10 medicine name suggestions.
     * Used to power the search-box dropdown in the frontend.
     */
    @GetMapping("/autocomplete")
    public ResponseEntity<ApiResponse<List<String>>> autocomplete(
            @RequestParam @NotBlank @Size(min = 2, max = 100) String q) {
        return ResponseEntity.ok(ApiResponse.ok(medicineService.autocomplete(q)));
    }
}
