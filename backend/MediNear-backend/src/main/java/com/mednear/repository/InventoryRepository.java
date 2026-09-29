package com.mednear.repository;

import com.mednear.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    List<Inventory> findByStore_StoreId(Long storeId);

    Optional<Inventory> findByStore_StoreIdAndMedicine_MedicineId(Long storeId, Long medicineId);

    @Modifying
    @Query("DELETE FROM Inventory i WHERE i.store.storeId = :storeId")
    void deleteAllByStoreId(@Param("storeId") Long storeId);
}
