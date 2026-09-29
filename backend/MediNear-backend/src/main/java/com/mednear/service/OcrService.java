package com.mednear.service;

import com.mednear.dto.response.FastApiOcrResponse;
import com.mednear.dto.response.OcrCandidateResponseDto;
import com.mednear.dto.response.OcrScanResponse;
import com.mednear.entity.MatchStatus;
import com.mednear.entity.Medicine;
import com.mednear.entity.OcrMedicine;
import com.mednear.entity.OcrScan;
import com.mednear.entity.User;
import com.mednear.exception.ResourceNotFoundException;
import com.mednear.repository.MedicineRepository;
import com.mednear.repository.OcrScanRepository;
import com.mednear.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class OcrService {

    @Autowired private FileStorageService fileStorageService;
    @Autowired private OcrClientService ocrClientService;
    @Autowired private MedicineMatchingService medicineMatchingService;
    @Autowired private OcrScanRepository ocrScanRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private MedicineRepository medicineRepository;

    @Transactional
    public OcrScanResponse scanPrescription(MultipartFile file, String userEmail) {
        // 1. Store the image locally on filesystem
        String imageUrl = fileStorageService.storeFile(file);

        // 2. Fetch logged-in user if available (guest scans have user = null)
        User user = null;
        if (userEmail != null && !userEmail.isBlank()) {
            user = userRepository.findByEmail(userEmail).orElse(null);
        }

        // 3. Call Python FastAPI /api/v1/analyze
        FastApiOcrResponse mlResult = ocrClientService.analyzeImage(file);

        String rawText = mlResult.getOcrText() != null ? mlResult.getOcrText() : "";
        String status = mlResult.isSuccess() ? "COMPLETED" : "FAILED";

        // 4. Resolve candidates against PostgreSQL medicines
        List<OcrCandidateResponseDto> candidateDtos = medicineMatchingService.resolveCandidates(mlResult.getMatches());

        // 5. Persist scan session & medicine records in PostgreSQL
        OcrScan scan = new OcrScan(user, imageUrl, rawText, status);

        for (OcrCandidateResponseDto cDto : candidateDtos) {
            Medicine med = null;
            if (cDto.getMedicineId() != null) {
                med = medicineRepository.findById(cDto.getMedicineId()).orElse(null);
            }
            MatchStatus mStatus = MatchStatus.valueOf(cDto.getMatchStatus());

            OcrMedicine ocrMed = new OcrMedicine(
                    med,
                    cDto.getMedicineName(),
                    cDto.getGenericName(),
                    cDto.getStrength(),
                    cDto.getMatchScore(),
                    mStatus
            );
            scan.addMedicine(ocrMed);
        }

        OcrScan savedScan = ocrScanRepository.save(scan);

        return new OcrScanResponse(
                savedScan.getId(),
                imageUrl,
                rawText,
                status,
                candidateDtos
        );
    }

    @Transactional(readOnly = true)
    public List<OcrScanResponse> getUserHistory(String email) {
        List<OcrScan> scans = ocrScanRepository.findByUser_EmailOrderByCreatedAtDesc(email);
        List<OcrScanResponse> responses = new ArrayList<>();

        for (OcrScan s : scans) {
            List<OcrCandidateResponseDto> candidates = s.getMedicines().stream().map(m -> new OcrCandidateResponseDto(
                    m.getMedicine() != null ? m.getMedicine().getMedicineId() : null,
                    m.getExtractedText(),
                    m.getGenericName(),
                    m.getStrength(),
                    m.getMatchScore(),
                    m.getMatchStatus().name()
            )).toList();

            responses.add(new OcrScanResponse(s.getId(), s.getImageUrl(), s.getRawText(), s.getStatus(), candidates));
        }

        return responses;
    }

    @Transactional(readOnly = true)
    public OcrScanResponse getScanById(Long scanId) {
        OcrScan s = ocrScanRepository.findById(scanId)
                .orElseThrow(() -> new ResourceNotFoundException("OcrScan", scanId));

        List<OcrCandidateResponseDto> candidates = s.getMedicines().stream().map(m -> new OcrCandidateResponseDto(
                m.getMedicine() != null ? m.getMedicine().getMedicineId() : null,
                m.getExtractedText(),
                m.getGenericName(),
                m.getStrength(),
                m.getMatchScore(),
                m.getMatchStatus().name()
        )).toList();

        return new OcrScanResponse(s.getId(), s.getImageUrl(), s.getRawText(), s.getStatus(), candidates);
    }
}
