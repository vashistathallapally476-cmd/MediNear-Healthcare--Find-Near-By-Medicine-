package com.mednear.service;

import com.mednear.dto.response.FastApiMatchCandidateDto;
import com.mednear.dto.response.OcrCandidateResponseDto;
import com.mednear.entity.MatchStatus;
import com.mednear.entity.Medicine;
import com.mednear.repository.MedicineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MedicineMatchingService {

    @Autowired
    private MedicineRepository medicineRepository;

    /**
     * Resolves ML candidates against PostgreSQL medicines table.
     * Matches by exact name, normalized keyword, or generic name.
     */
    public List<OcrCandidateResponseDto> resolveCandidates(List<FastApiMatchCandidateDto> mlCandidates) {
        List<OcrCandidateResponseDto> results = new ArrayList<>();
        if (mlCandidates == null || mlCandidates.isEmpty()) {
            return results;
        }

        for (FastApiMatchCandidateDto candidate : mlCandidates) {
            String rawName = candidate.getName() != null ? candidate.getName().trim() : "";
            Double score = candidate.getMatchScore() != null ? candidate.getMatchScore() : 0.0;
            String generic = candidate.getGenericName();
            String strength = candidate.getStrength();

            Optional<Medicine> matchedMedicine = findInDatabase(rawName, generic);

            OcrCandidateResponseDto dto = new OcrCandidateResponseDto();
            dto.setGenericName(generic);
            dto.setStrength(strength);
            dto.setMatchScore(score);

            if (matchedMedicine.isPresent()) {
                Medicine m = matchedMedicine.get();
                dto.setMedicineId(m.getMedicineId());
                dto.setMedicineName(m.getMedicineName());
                // Score threshold 0.4 separates high confidence from low confidence
                dto.setMatchStatus(score >= 0.40 ? MatchStatus.MATCHED.name() : MatchStatus.LOW_CONFIDENCE.name());
            } else {
                dto.setMedicineId(null);
                dto.setMedicineName(rawName);
                dto.setMatchStatus(MatchStatus.NOT_FOUND.name());
            }

            results.add(dto);
        }

        return results;
    }

    private Optional<Medicine> findInDatabase(String name, String genericName) {
        if (name.isBlank()) return Optional.empty();

        // 1. Direct match (e.g. "DOLO 650" or "PARACETAMOL 500MG")
        Optional<Medicine> direct = medicineRepository.findByMedicineNameIgnoreCase(name);
        if (direct.isPresent()) return direct;

        // 2. Keyword substring search (e.g. "Dolo" in "Dolo 650 Tablet")
        String cleanFirstWord = name.split("[\\s\\-_]+")[0];
        if (cleanFirstWord.length() >= 3) {
            List<String> suggestions = medicineRepository.findNamesByKeyword(cleanFirstWord, PageRequest.of(0, 1));
            if (!suggestions.isEmpty()) {
                Optional<Medicine> byKeyword = medicineRepository.findByMedicineNameIgnoreCase(suggestions.get(0));
                if (byKeyword.isPresent()) return byKeyword;
            }
        }

        // 3. Fallback to generic name search if provided
        if (genericName != null && genericName.trim().length() >= 3) {
            List<String> genericMatches = medicineRepository.findNamesByKeyword(genericName.trim(), PageRequest.of(0, 1));
            if (!genericMatches.isEmpty()) {
                return medicineRepository.findByMedicineNameIgnoreCase(genericMatches.get(0));
            }
        }

        return Optional.empty();
    }
}
