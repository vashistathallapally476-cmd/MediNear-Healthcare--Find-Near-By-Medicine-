package com.mednear.repository;

import com.mednear.entity.Store;
import com.mednear.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

    List<Store> findByOwnerAndActiveTrue(User owner);

    /**
     * ─────────────────────────────────────────────────────────────
     *  HAVERSINE SEARCH QUERY
     * ─────────────────────────────────────────────────────────────
     *  Improvements over the original:
     *
     *  1. LEAST(1.0, ...) guard  – prevents NaN from ACOS domain error
     *     when two points are identical (floating-point rounding can
     *     produce values like 1.0000000002).
     *
     *  2. ROUND(..., 2)          – distance returned as e.g. 2.47 km,
     *     not 2.4731849238...
     *
     *  3. :radiusKm parameter    – caller controls radius (default 5km,
     *     max 50km), making this reusable without query duplication.
     *
     *  4. s.is_active = true     – excludes soft-deleted stores.
     *
     *  5. Separate countQuery    – required by Spring Data for Page<T>
     *     pagination with native queries; avoids a broken auto-count.
     *
     *  6. Returns NearbyStoreProjection – typed projection, not Object[].
     *
     *  Index usage (explain analyse):
     *    idx_inv_medicine   → covers the medicine JOIN + ILIKE filter
     *    idx_inv_store      → covers the stores JOIN
     *    idx_inv_qty        → covers quantity > 0 filter
     *    idx_stores_active  → covers is_active = true
     *    idx_stores_lat/lng → used by planner for geo bounding estimate
     * ─────────────────────────────────────────────────────────────
     */
    @Query(
        value = """
            SELECT
                s.store_id        AS storeId,
                s.store_name      AS storeName,
                s.address         AS address,
                s.phone           AS phone,
                s.latitude        AS latitude,
                s.longitude       AS longitude,
                i.quantity        AS quantity,
                i.last_updated    AS lastUpdated,
                m.medicine_id     AS medicineId,
                m.medicine_name   AS medicineName,
                ROUND(CAST(
                    6371 * ACOS(
                        LEAST(1.0,
                            COS(RADIANS(:userLat))
                          * COS(RADIANS(s.latitude))
                          * COS(RADIANS(s.longitude) - RADIANS(:userLng))
                          + SIN(RADIANS(:userLat))
                          * SIN(RADIANS(s.latitude))
                        )
                    ) AS NUMERIC
                ), 2) AS distanceKm
            FROM   stores s
            JOIN   inventory i  ON i.store_id    = s.store_id
            JOIN   medicines m  ON m.medicine_id = i.medicine_id
            WHERE  m.medicine_name ILIKE CONCAT('%', :medicineName, '%')
              AND  i.quantity    > 0
              AND  s.is_active   = true
              AND  6371 * ACOS(
                       LEAST(1.0,
                           COS(RADIANS(:userLat))
                         * COS(RADIANS(s.latitude))
                         * COS(RADIANS(s.longitude) - RADIANS(:userLng))
                         + SIN(RADIANS(:userLat))
                         * SIN(RADIANS(s.latitude))
                       )
                   ) <= :radiusKm
            ORDER  BY distanceKm ASC
            """,
        countQuery = """
            SELECT COUNT(*)
            FROM   stores s
            JOIN   inventory i  ON i.store_id    = s.store_id
            JOIN   medicines m  ON m.medicine_id = i.medicine_id
            WHERE  m.medicine_name ILIKE CONCAT('%', :medicineName, '%')
              AND  i.quantity    > 0
              AND  s.is_active   = true
              AND  6371 * ACOS(
                       LEAST(1.0,
                           COS(RADIANS(:userLat))
                         * COS(RADIANS(s.latitude))
                         * COS(RADIANS(s.longitude) - RADIANS(:userLng))
                         + SIN(RADIANS(:userLat))
                         * SIN(RADIANS(s.latitude))
                       )
                   ) <= :radiusKm
            """,
        nativeQuery = true
    )
    Page<NearbyStoreProjection> findNearbyStoresWithMedicine(
            @Param("medicineName") String medicineName,
            @Param("userLat")      double userLat,
            @Param("userLng")      double userLng,
            @Param("radiusKm")     double radiusKm,
            Pageable pageable
    );
}
