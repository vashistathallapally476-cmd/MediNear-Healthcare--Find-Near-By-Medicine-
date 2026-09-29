package com.mednear.controller;

import com.mednear.dto.response.ApiResponse;
import com.mednear.dto.response.OcrScanResponse;
import com.mednear.service.OcrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/ocr")
public class OcrController {

    @Autowired
    private OcrService ocrService;

    /**
     * POST /api/ocr/scan
     * Uploads prescription image, extracts text via Python OCR/ML,
     * matches against PostgreSQL medicines table, and returns candidates.
     */
    @PostMapping(value = "/scan", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<OcrScanResponse>> scan(
            @RequestParam("image") MultipartFile image,
            @AuthenticationPrincipal UserDetails currentUser) {

        String userEmail = currentUser != null ? currentUser.getUsername() : null;
        OcrScanResponse data = ocrService.scanPrescription(image, userEmail);

        return ResponseEntity.ok(ApiResponse.ok("Prescription scanned successfully", data));
    }

    /**
     * GET /api/ocr/history
     * Returns past scans for the currently logged-in user.
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<OcrScanResponse>>> history(
            @AuthenticationPrincipal UserDetails currentUser) {

        if (currentUser == null) {
            return ResponseEntity.ok(ApiResponse.ok(List.of()));
        }

        List<OcrScanResponse> data = ocrService.getUserHistory(currentUser.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    /**
     * GET /api/ocr/scan/{id}
     * Returns details of a specific scan.
     */
    @GetMapping("/scan/{id}")
    public ResponseEntity<ApiResponse<OcrScanResponse>> getScanById(@PathVariable Long id) {
        OcrScanResponse data = ocrService.getScanById(id);
        return ResponseEntity.ok(ApiResponse.ok(data));
    }
}
