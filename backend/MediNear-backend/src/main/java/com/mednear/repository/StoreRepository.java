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
     * Haversine search query for finding nearby pharmacies
     * that have the requested medicine in stock.
     *
     * The query also returns the pharmacy image URL from
     * stores.image_url so the customer frontend can display it.
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
                s.image_url       AS imageUrl,
                i.quantity        AS quantity,
                i.last_updated    AS lastUpdated,
                m.medicine_id     AS medicineId,
                m.medicine_name   AS medicineName,
                ROUND(CAST(
                    6371 * ACOS(
                        LEAST(
                            1.0,
                            COS(RADIANS(:userLat))
                            * COS(RADIANS(s.latitude))
                            * COS(
                                RADIANS(s.longitude)
                                - RADIANS(:userLng)
                            )
                            + SIN(RADIANS(:userLat))
                            * SIN(RADIANS(s.latitude))
                        )
                    ) AS NUMERIC
                ), 2) AS distanceKm
            FROM stores s
            JOIN inventory i
                ON i.store_id = s.store_id
            JOIN medicines m
                ON m.medicine_id = i.medicine_id
            WHERE m.medicine_name ILIKE CONCAT('%', :medicineName, '%')
              AND i.quantity > 0
              AND s.is_active = true
              AND 6371 * ACOS(
                    LEAST(
                        1.0,
                        COS(RADIANS(:userLat))
                        * COS(RADIANS(s.latitude))
                        * COS(
                            RADIANS(s.longitude)
                            - RADIANS(:userLng)
                        )
                        + SIN(RADIANS(:userLat))
                        * SIN(RADIANS(s.latitude))
                    )
                  ) <= :radiusKm
            ORDER BY distanceKm ASC
            """,

        countQuery = """
            SELECT COUNT(*)
            FROM stores s
            JOIN inventory i
                ON i.store_id = s.store_id
            JOIN medicines m
                ON m.medicine_id = i.medicine_id
            WHERE m.medicine_name ILIKE CONCAT('%', :medicineName, '%')
              AND i.quantity > 0
              AND s.is_active = true
              AND 6371 * ACOS(
                    LEAST(
                        1.0,
                        COS(RADIANS(:userLat))
                        * COS(RADIANS(s.latitude))
                        * COS(
                            RADIANS(s.longitude)
                            - RADIANS(:userLng)
                        )
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