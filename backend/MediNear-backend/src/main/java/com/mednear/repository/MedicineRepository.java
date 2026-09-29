package com.mednear.repository;

import com.mednear.entity.Medicine;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    Optional<Medicine> findByMedicineNameIgnoreCase(String name);

    /**
     * Autocomplete – prefix match (fast with B-tree idx_medicines_name).
     * Called by GET /api/medicines/autocomplete?q=para → returns ["PARACETAMOL 500", ...]
     */
    @Query("""
        SELECT m.medicineName
        FROM   Medicine m
        WHERE  LOWER(m.medicineName) LIKE LOWER(CONCAT(:prefix, '%'))
        ORDER  BY m.medicineName ASC
        """)
    List<String> findNamesByPrefix(@Param("prefix") String prefix, Pageable pageable);

    /**
     * Broader keyword suggestion – substring match anywhere in name.
     * Called when prefix search returns < 3 results.
     */
    @Query("""
        SELECT m.medicineName
        FROM   Medicine m
        WHERE  LOWER(m.medicineName) LIKE LOWER(CONCAT('%', :keyword, '%'))
        ORDER  BY m.medicineName ASC
        """)
    List<String> findNamesByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
