package com.mednear.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Store (pharmacy) entity.
 *
 * Indexes:
 *   idx_stores_owner     – filters by owner on /api/stores/my
 *   idx_stores_lat_lng   – composite; used in Haversine bounding-box pre-filter
 *   idx_stores_active    – WHERE is_active = true on every search
 */
@Entity
@Table(
    name = "stores",
    indexes = {
        @Index(name = "idx_stores_owner",   columnList = "owner_id"),
        @Index(name = "idx_stores_lat",     columnList = "latitude"),
        @Index(name = "idx_stores_lng",     columnList = "longitude"),
        @Index(name = "idx_stores_active",  columnList = "is_active")
    }
)
@EntityListeners(AuditingEntityListener.class)
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long storeId;

    @NotBlank
    @Size(max = 200)
    @Column(name = "store_name", nullable = false, length = 200)
    private String storeName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @NotBlank
    @Size(max = 500)
    @Column(nullable = false, length = 500)
    private String address;

    @NotNull
    @Column(nullable = false)
    private Double latitude;

    @NotNull
    @Column(nullable = false)
    private Double longitude;

    @NotBlank
    @Size(max = 20)
    @Column(nullable = false, length = 20)
    private String phone;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public Store() {}

    public Store(String storeName, User owner, String address,
                 Double latitude, Double longitude, String phone) {
        this.storeName = storeName;
        this.owner     = owner;
        this.address   = address;
        this.latitude  = latitude;
        this.longitude = longitude;
        this.phone     = phone;
    }

    public Long          getStoreId()            { return storeId; }
    public void          setStoreId(Long id)     { this.storeId = id; }
    public String        getStoreName()          { return storeName; }
    public void          setStoreName(String v)  { this.storeName = v; }
    public User          getOwner()              { return owner; }
    public void          setOwner(User v)        { this.owner = v; }
    public String        getAddress()            { return address; }
    public void          setAddress(String v)    { this.address = v; }
    public Double        getLatitude()           { return latitude; }
    public void          setLatitude(Double v)   { this.latitude = v; }
    public Double        getLongitude()          { return longitude; }
    public void          setLongitude(Double v)  { this.longitude = v; }
    public String        getPhone()              { return phone; }
    public void          setPhone(String v)      { this.phone = v; }
    public boolean       isActive()              { return active; }
    public void          setActive(boolean v)    { this.active = v; }
    public LocalDateTime getCreatedAt()          { return createdAt; }
}
