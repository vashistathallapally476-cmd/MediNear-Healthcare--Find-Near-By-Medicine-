package com.mednear.repository;

import java.time.LocalDateTime;

/**
 * Spring Data Projection — maps every aliased column from the
 * Haversine native query in StoreRepository.
 *
 * Rules:
 *  • Getter name MUST match the SQL alias exactly (camelCase).
 *  • Spring Data handles type conversion automatically.
 *  • No casting, no Object[] unpacking — ever.
 */
public interface NearbyStoreProjection {
    Long          getStoreId();
    String        getStoreName();
    String        getAddress();
    String        getPhone();
    Double        getLatitude();
    Double        getLongitude();
    Integer       getQuantity();
    LocalDateTime getLastUpdated();
    Long          getMedicineId();
    String        getMedicineName();
    Double        getDistanceKm();     // aliased AS distanceKm in SQL
}
