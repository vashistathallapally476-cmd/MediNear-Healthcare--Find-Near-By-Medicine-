package com.mednear.dto.response;

import com.mednear.entity.Inventory;
import java.time.LocalDateTime;

public class InventoryResponse {
    private Long          inventoryId;
    private Long          medicineId;
    private String        medicineName;
    private Integer       quantity;
    private String        stockStatus;
    private LocalDateTime lastUpdated;

    public static InventoryResponse from(Inventory i) {
        InventoryResponse r = new InventoryResponse();
        r.inventoryId  = i.getInventoryId();
        r.medicineId   = i.getMedicine().getMedicineId();
        r.medicineName = i.getMedicine().getMedicineName();
        r.quantity     = i.getQuantity();
        r.stockStatus  = resolveStatus(i.getQuantity());
        r.lastUpdated  = i.getLastUpdated();
        return r;
    }

    private static String resolveStatus(int qty) {
        if (qty == 0)  return "OUT_OF_STOCK";
        if (qty < 10)  return "CRITICAL";
        if (qty < 50)  return "LOW";
        return "HIGH";
    }

    public Long          getInventoryId()  { return inventoryId; }
    public Long          getMedicineId()   { return medicineId; }
    public String        getMedicineName() { return medicineName; }
    public Integer       getQuantity()     { return quantity; }
    public String        getStockStatus()  { return stockStatus; }
    public LocalDateTime getLastUpdated()  { return lastUpdated; }
}
