package com.mednear.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Inventory: links a Store to a Medicine with a stock quantity.
 *
 * Indexes:
 *   idx_inv_store      – JOIN with stores in Haversine query
 *   idx_inv_medicine   – JOIN with medicines + medicine name ILIKE filter
 *   idx_inv_qty        – WHERE quantity > 0 filter
 *
 * Composite unique constraint prevents duplicate (store, medicine) rows.
 */
@Entity
@Table(
    name = "inventory",
    uniqueConstraints = {
        @UniqueConstraint(
            name        = "uk_inventory_store_medicine",
            columnNames = {"store_id", "medicine_id"}
        )
    },
    indexes = {
        @Index(name = "idx_inv_store",    columnList = "store_id"),
        @Index(name = "idx_inv_medicine", columnList = "medicine_id"),
        @Index(name = "idx_inv_qty",      columnList = "quantity")
    }
)
@EntityListeners(AuditingEntityListener.class)
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long inventoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @Min(0)
    @Column(nullable = false)
    private Integer quantity;

    @LastModifiedDate
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    public Inventory() {}

    public Inventory(Store store, Medicine medicine, Integer quantity) {
        this.store    = store;
        this.medicine = medicine;
        this.quantity = quantity;
    }

    @PrePersist
    public void prePersist() {
        if (this.lastUpdated == null) this.lastUpdated = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.lastUpdated = LocalDateTime.now();
    }

    public Long          getInventoryId()              { return inventoryId; }
    public void          setInventoryId(Long id)       { this.inventoryId = id; }
    public Store         getStore()                    { return store; }
    public void          setStore(Store v)             { this.store = v; }
    public Medicine      getMedicine()                 { return medicine; }
    public void          setMedicine(Medicine v)       { this.medicine = v; }
    public Integer       getQuantity()                 { return quantity; }
    public void          setQuantity(Integer v)        { this.quantity = v; }
    public LocalDateTime getLastUpdated()              { return lastUpdated; }
    public void          setLastUpdated(LocalDateTime v){ this.lastUpdated = v; }
}
