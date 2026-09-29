package com.mednear.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "ocr_scans",
    indexes = {
        @Index(name = "idx_ocr_scans_user_id", columnList = "user_id")
    }
)
@EntityListeners(AuditingEntityListener.class)
public class OcrScan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "raw_text", columnDefinition = "TEXT")
    private String rawText;

    @Column(nullable = false, length = 50)
    private String status = "COMPLETED";

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "scan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OcrMedicine> medicines = new ArrayList<>();

    public OcrScan() {}

    public OcrScan(User user, String imageUrl, String rawText, String status) {
        this.user = user;
        this.imageUrl = imageUrl;
        this.rawText = rawText;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getRawText() { return rawText; }
    public void setRawText(String rawText) { this.rawText = rawText; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<OcrMedicine> getMedicines() { return medicines; }
    public void setMedicines(List<OcrMedicine> medicines) { this.medicines = medicines; }

    public void addMedicine(OcrMedicine med) {
        medicines.add(med);
        med.setScan(this);
    }
}
