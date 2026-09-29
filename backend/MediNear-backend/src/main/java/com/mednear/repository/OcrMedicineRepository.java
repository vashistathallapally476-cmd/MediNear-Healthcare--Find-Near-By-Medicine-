package com.mednear.repository;

import com.mednear.entity.OcrMedicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OcrMedicineRepository extends JpaRepository<OcrMedicine, Long> {
    List<OcrMedicine> findByScan_Id(Long scanId);
}
