package com.mednear.controller;

import com.mednear.dto.request.StoreRequest;
import com.mednear.dto.response.ApiResponse;
import com.mednear.dto.response.StoreResponse;
import com.mednear.service.StoreService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/stores")
public class StoreController {

    @Autowired
    private StoreService storeService;

    /**
     * POST /api/stores
     *
     * Register a new pharmacy.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<StoreResponse>> register(
            @Valid @RequestBody StoreRequest req,
            @AuthenticationPrincipal UserDetails currentUser
    ) {

        StoreResponse data =
            storeService.registerStore(
                req,
                currentUser.getUsername()
            );

        return ResponseEntity
            .status(201)
            .body(
                ApiResponse.ok(
                    "Store registered",
                    data
                )
            );
    }

    /**
     * GET /api/stores/my
     *
     * Get all stores owned by current owner.
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<StoreResponse>>> myStores(
            @AuthenticationPrincipal UserDetails currentUser
    ) {

        return ResponseEntity.ok(
            ApiResponse.ok(
                storeService.getMyStores(
                    currentUser.getUsername()
                )
            )
        );
    }

    /**
     * POST /api/stores/{id}/image
     *
     * Upload pharmacy image.
     *
     * Multipart field:
     * image
     */
    @PostMapping("/{id}/image")
    public ResponseEntity<ApiResponse<StoreResponse>> uploadImage(
            @PathVariable Long id,
            @RequestPart("image") MultipartFile image,
            @AuthenticationPrincipal UserDetails currentUser
    ) throws IOException {

        StoreResponse data =
            storeService.uploadPharmacyImage(
                id,
                image,
                currentUser.getUsername()
            );

        return ResponseEntity.ok(
            ApiResponse.ok(
                "Pharmacy image uploaded",
                data
            )
        );
    }

    /**
     * DELETE /api/stores/{id}
     *
     * Soft-delete a pharmacy.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deactivate(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser
    ) {

        storeService.deactivateStore(
            id,
            currentUser.getUsername()
        );

        return ResponseEntity.ok(
            ApiResponse.ok(
                "Store deactivated",
                null
            )
        );
    }
}