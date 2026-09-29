package com.mednear.repository;

import com.mednear.entity.OcrScan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OcrScanRepository extends JpaRepository<OcrScan, Long> {
    List<OcrScan> findByUser_EmailOrderByCreatedAtDesc(String email);
}
