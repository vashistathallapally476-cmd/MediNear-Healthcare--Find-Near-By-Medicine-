package com.mednear.controller;

import com.mednear.dto.request.InventoryRequest;
import com.mednear.dto.response.ApiResponse;
import com.mednear.dto.response.InventoryResponse;
import com.mednear.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired private InventoryService inventoryService;

    /** PUT /api/inventory  — upsert stock for a medicine in a store */
    @PutMapping
    public ResponseEntity<ApiResponse<InventoryResponse>> upsert(
            @Valid @RequestBody InventoryRequest req,
            @AuthenticationPrincipal UserDetails currentUser) {
        InventoryResponse data = inventoryService.upsertInventory(req, currentUser.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Inventory updated", data));
    }

    /** GET /api/inventory/store/{storeId}  — list all stock for a store */
    @GetMapping("/store/{storeId}")
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> list(
            @PathVariable Long storeId,
            @AuthenticationPrincipal UserDetails currentUser) {
        List<InventoryResponse> data =
            inventoryService.getStoreInventory(storeId, currentUser.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    /** DELETE /api/inventory/{inventoryId}  — remove one inventory record */
    @DeleteMapping("/{inventoryId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long inventoryId,
            @AuthenticationPrincipal UserDetails currentUser) {
        inventoryService.deleteInventory(inventoryId, currentUser.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Inventory record deleted", null));
    }
}
