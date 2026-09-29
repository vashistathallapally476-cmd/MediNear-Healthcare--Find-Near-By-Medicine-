package com.mednear.dto.response;

public class OcrCandidateResponseDto {

    private Long medicineId;
    private String medicineName;
    private String genericName;
    private String strength;
    private Double matchScore;
    private String matchStatus;

    public OcrCandidateResponseDto() {}

    public OcrCandidateResponseDto(Long medicineId, String medicineName, String genericName,
                                   String strength, Double matchScore, String matchStatus) {
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.genericName = genericName;
        this.strength = strength;
        this.matchScore = matchScore;
        this.matchStatus = matchStatus;
    }

    public Long getMedicineId() { return medicineId; }
    public void setMedicineId(Long medicineId) { this.medicineId = medicineId; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public String getGenericName() { return genericName; }
    public void setGenericName(String genericName) { this.genericName = genericName; }

    public String getStrength() { return strength; }
    public void setStrength(String strength) { this.strength = strength; }

    public Double getMatchScore() { return matchScore; }
    public void setMatchScore(Double matchScore) { this.matchScore = matchScore; }

    public String getMatchStatus() { return matchStatus; }
    public void setMatchStatus(String matchStatus) { this.matchStatus = matchStatus; }
}
