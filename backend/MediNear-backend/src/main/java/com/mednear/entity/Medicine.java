package com.mednear.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Medicine catalogue entry.
 *
 * Indexes:
 *   idx_medicines_name  – B-tree on medicine_name, used in ILIKE search
 *                         and autocomplete prefix queries.
 *
 * For autocomplete at scale, add in DB_SCHEMA.sql:
 *   CREATE INDEX idx_medicines_name_gin
 *     ON medicines USING gin (to_tsvector('english', medicine_name));
 */
@Entity
@Table(
    name = "medicines",
    indexes = {
        @Index(name = "idx_medicines_name", columnList = "medicine_name")
    }
)
public class Medicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long medicineId;

    @NotBlank
    @Size(max = 255)
    @Column(name = "medicine_name", nullable = false, unique = true, length = 255)
    private String medicineName;

    // Future-ready fields (nullable so existing data is untouched)
    @Size(max = 100)
    @Column(length = 100)
    private String category;

    @Column(name = "is_prescription_required")
    private Boolean prescriptionRequired = false;

    public Medicine() {}

    public Medicine(String medicineName) {
        this.medicineName = medicineName;
    }

    public Long    getMedicineId()                   { return medicineId; }
    public void    setMedicineId(Long id)            { this.medicineId = id; }
    public String  getMedicineName()                 { return medicineName; }
    public void    setMedicineName(String v)         { this.medicineName = v; }
    public String  getCategory()                     { return category; }
    public void    setCategory(String v)             { this.category = v; }
    public Boolean isPrescriptionRequired()          { return prescriptionRequired; }
    public void    setPrescriptionRequired(Boolean v){ this.prescriptionRequired = v; }
}
