package com.mednear.entity;

import jakarta.persistence.*;

@Entity
@Table(
    name = "ocr_medicines",
    indexes = {
        @Index(name = "idx_ocr_medicines_scan_id", columnList = "ocr_scan_id")
    }
)
public class OcrMedicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ocr_scan_id", nullable = false)
    private OcrScan scan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id")
    private Medicine medicine;

    @Column(name = "extracted_text", nullable = false, length = 255)
    private String extractedText;

    @Column(name = "generic_name", length = 255)
    private String genericName;

    @Column(length = 100)
    private String strength;

    @Column(name = "match_score", nullable = false)
    private Double matchScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "match_status", nullable = false, length = 50)
    private MatchStatus matchStatus;

    public OcrMedicine() {}

    public OcrMedicine(Medicine medicine, String extractedText, String genericName,
                       String strength, Double matchScore, MatchStatus matchStatus) {
        this.medicine = medicine;
        this.extractedText = extractedText;
        this.genericName = genericName;
        this.strength = strength;
        this.matchScore = matchScore;
        this.matchStatus = matchStatus;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public OcrScan getScan() { return scan; }
    public void setScan(OcrScan scan) { this.scan = scan; }

    public Medicine getMedicine() { return medicine; }
    public void setMedicine(Medicine medicine) { this.medicine = medicine; }

    public String getExtractedText() { return extractedText; }
    public void setExtractedText(String extractedText) { this.extractedText = extractedText; }

    public String getGenericName() { return genericName; }
    public void setGenericName(String genericName) { this.genericName = genericName; }

    public String getStrength() { return strength; }
    public void setStrength(String strength) { this.strength = strength; }

    public Double getMatchScore() { return matchScore; }
    public void setMatchScore(Double matchScore) { this.matchScore = matchScore; }

    public MatchStatus getMatchStatus() { return matchStatus; }
    public void setMatchStatus(MatchStatus matchStatus) { this.matchStatus = matchStatus; }
}
