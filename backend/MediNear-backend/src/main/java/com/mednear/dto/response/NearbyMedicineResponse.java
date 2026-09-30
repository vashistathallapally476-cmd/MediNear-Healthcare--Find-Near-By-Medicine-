package com.mednear.dto.response;

import com.mednear.repository.NearbyStoreProjection;
import java.time.LocalDateTime;

/**
 * One search result row for GET /api/medicines/nearby.
 *
 * Built from NearbyStoreProjection — no casting, no Object[].
 *
 * stockStatus:
 *   HIGH     → qty >= 50
 *   LOW      → qty 10-49
 *   CRITICAL → qty 1-9
 */
public class NearbyMedicineResponse {

    private Long          storeId;
    private String        storeName;
    private String        address;
    private String        phone;
    private Double        latitude;
    private Double        longitude;
    private String        imageUrl;
    private Double        distanceKm;
    private Integer       quantity;
    private String        stockStatus;
    private Long          medicineId;
    private String        medicineName;
    private LocalDateTime lastUpdated;

    public static NearbyMedicineResponse from(NearbyStoreProjection p) {

        NearbyMedicineResponse r = new NearbyMedicineResponse();

        r.storeId      = p.getStoreId();
        r.storeName    = p.getStoreName();
        r.address      = p.getAddress();
        r.phone        = p.getPhone();
        r.latitude     = p.getLatitude();
        r.longitude    = p.getLongitude();

        // Pharmacy image from Supabase Storage
        r.imageUrl     = p.getImageUrl();

        r.distanceKm   = p.getDistanceKm();
        r.quantity     = p.getQuantity();
        r.stockStatus  = resolveStockStatus(p.getQuantity());
        r.medicineId   = p.getMedicineId();
        r.medicineName = p.getMedicineName();
        r.lastUpdated  = p.getLastUpdated();

        return r;
    }

    private static String resolveStockStatus(int qty) {
        if (qty >= 50) return "HIGH";
        if (qty >= 10) return "LOW";
        return "CRITICAL";
    }

    public Long getStoreId() {
        return storeId;
    }

    public String getStoreName() {
        return storeName;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public String getStockStatus() {
        return stockStatus;
    }

    public Long getMedicineId() {
        return medicineId;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
}