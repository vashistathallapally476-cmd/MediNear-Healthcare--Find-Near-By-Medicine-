package com.mednear.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class InventoryRequest {

    @NotNull(message = "Store ID is required")
    private Long storeId;

    @NotBlank(message = "Medicine name is required")
    @Size(max = 255, message = "Medicine name too long")
    private String medicineName;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;

    public Long    getStoreId()              { return storeId; }
    public void    setStoreId(Long v)        { this.storeId = v; }
    public String  getMedicineName()         { return medicineName; }
    public void    setMedicineName(String v) { this.medicineName = v; }
    public Integer getQuantity()             { return quantity; }
    public void    setQuantity(Integer v)    { this.quantity = v; }
}
