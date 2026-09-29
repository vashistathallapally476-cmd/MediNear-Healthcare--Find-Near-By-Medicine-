package com.mednear.dto.response;

import java.time.LocalDateTime;

/**
 * WebSocket event published to /topic/inventory/{storeId}
 * whenever an owner updates stock.
 *
 * Frontend receives this and updates the UI instantly —
 * no page refresh, no polling.
 */
public class InventoryUpdateEvent {

    private Long          storeId;
    private String        storeName;
    private Long          medicineId;
    private String        medicineName;
    private Integer       newQuantity;
    private String        stockStatus;
    private String        eventType;       // "UPDATED" | "OUT_OF_STOCK" | "RESTOCKED"
    private LocalDateTime updatedAt;

    public InventoryUpdateEvent() {}

    public InventoryUpdateEvent(Long storeId, String storeName,
                                Long medicineId, String medicineName,
                                Integer newQuantity) {
        this.storeId      = storeId;
        this.storeName    = storeName;
        this.medicineId   = medicineId;
        this.medicineName = medicineName;
        this.newQuantity  = newQuantity;
        this.stockStatus  = resolveStatus(newQuantity);
        this.eventType    = resolveEvent(newQuantity);
        this.updatedAt    = LocalDateTime.now();
    }

    private static String resolveStatus(int qty) {
        if (qty == 0)  return "OUT_OF_STOCK";
        if (qty < 10)  return "CRITICAL";
        if (qty < 50)  return "LOW";
        return "HIGH";
    }

    private static String resolveEvent(int qty) {
        if (qty == 0) return "OUT_OF_STOCK";
        if (qty > 0)  return "RESTOCKED";
        return "UPDATED";
    }

    public Long          getStoreId()      { return storeId; }
    public String        getStoreName()    { return storeName; }
    public Long          getMedicineId()   { return medicineId; }
    public String        getMedicineName() { return medicineName; }
    public Integer       getNewQuantity()  { return newQuantity; }
    public String        getStockStatus()  { return stockStatus; }
    public String        getEventType()    { return eventType; }
    public LocalDateTime getUpdatedAt()    { return updatedAt; }
}
