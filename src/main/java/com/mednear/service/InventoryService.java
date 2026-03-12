package com.mednear.service;

import com.mednear.dto.request.InventoryRequest;
import com.mednear.dto.response.InventoryResponse;
import com.mednear.dto.response.InventoryUpdateEvent;
import com.mednear.entity.Inventory;
import com.mednear.entity.Medicine;
import com.mednear.entity.Store;
import com.mednear.exception.ResourceNotFoundException;
import com.mednear.exception.UnauthorizedException;
import com.mednear.repository.InventoryRepository;
import com.mednear.repository.MedicineRepository;
import com.mednear.repository.StoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Inventory service — upsert, list, delete stock.
 *
 * After every upsert / delete:
 *   1. Broadcasts InventoryUpdateEvent via WebSocket → /topic/inventory/{storeId}
 *      All customers subscribed to that store see the update live.
 *
 *   2. Evicts the Redis medicineSearch cache via MedicineService.evictSearchCache()
 *      so the next search reflects the updated stock immediately.
 *
 * This is the same pattern used by food delivery apps:
 *   Restaurant marks item "out of stock" → all open app instances update instantly.
 */
@Service
public class InventoryService {

    @Autowired private InventoryRepository   inventoryRepository;
    @Autowired private MedicineRepository    medicineRepository;
    @Autowired private StoreRepository       storeRepository;
    @Autowired private SimpMessagingTemplate messagingTemplate;  // WebSocket
    @Autowired private MedicineService       medicineService;    // cache eviction

    @Transactional
    public InventoryResponse upsertInventory(InventoryRequest req, String ownerEmail) {
        Store store = getOwnedStore(req.getStoreId(), ownerEmail);

        String normalised = req.getMedicineName().trim().toUpperCase();
        Medicine medicine = medicineRepository.findByMedicineNameIgnoreCase(normalised)
            .orElseGet(() -> medicineRepository.save(new Medicine(normalised)));

        Inventory inventory = inventoryRepository
            .findByStore_StoreIdAndMedicine_MedicineId(
                store.getStoreId(), medicine.getMedicineId())
            .orElseGet(() -> new Inventory(store, medicine, 0));

        inventory.setQuantity(req.getQuantity());
        Inventory saved = inventoryRepository.save(inventory);

        // 1. Real-time WebSocket broadcast
        broadcastUpdate(store, medicine, req.getQuantity());

        // 2. Evict Redis cache — next search will hit DB + re-cache
        medicineService.evictSearchCache();

        return InventoryResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> getStoreInventory(Long storeId, String ownerEmail) {
        Store store = getOwnedStore(storeId, ownerEmail);
        return inventoryRepository.findByStore_StoreId(store.getStoreId())
            .stream().map(InventoryResponse::from).toList();
    }

    @Transactional
    public void deleteInventory(Long inventoryId, String ownerEmail) {
        Inventory inv = inventoryRepository.findById(inventoryId)
            .orElseThrow(() -> new ResourceNotFoundException("Inventory", inventoryId));
        if (!inv.getStore().getOwner().getEmail().equals(ownerEmail)) {
            throw new UnauthorizedException("You do not own this inventory record");
        }
        Store    store    = inv.getStore();
        Medicine medicine = inv.getMedicine();
        inventoryRepository.delete(inv);

        broadcastUpdate(store, medicine, 0);
        medicineService.evictSearchCache();
    }

    // ── Private helpers ─────────────────────────────────────────

    private void broadcastUpdate(Store store, Medicine medicine, int quantity) {
        InventoryUpdateEvent event = new InventoryUpdateEvent(
            store.getStoreId(), store.getStoreName(),
            medicine.getMedicineId(), medicine.getMedicineName(), quantity);
        messagingTemplate.convertAndSend(
            "/topic/inventory/" + store.getStoreId(), event);
    }

    private Store getOwnedStore(Long storeId, String ownerEmail) {
        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new ResourceNotFoundException("Store", storeId));
        if (!store.getOwner().getEmail().equals(ownerEmail)) {
            throw new UnauthorizedException("You do not own store " + storeId);
        }
        return store;
    }
}
