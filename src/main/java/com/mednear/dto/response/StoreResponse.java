package com.mednear.dto.response;

import com.mednear.entity.Store;

public class StoreResponse {
    private Long    storeId;
    private String  storeName;
    private String  address;
    private String  phone;
    private Double  latitude;
    private Double  longitude;
    private boolean active;

    public static StoreResponse from(Store s) {
        StoreResponse r = new StoreResponse();
        r.storeId   = s.getStoreId();
        r.storeName = s.getStoreName();
        r.address   = s.getAddress();
        r.phone     = s.getPhone();
        r.latitude  = s.getLatitude();
        r.longitude = s.getLongitude();
        r.active    = s.isActive();
        return r;
    }

    public Long    getStoreId()   { return storeId; }
    public String  getStoreName() { return storeName; }
    public String  getAddress()   { return address; }
    public String  getPhone()     { return phone; }
    public Double  getLatitude()  { return latitude; }
    public Double  getLongitude() { return longitude; }
    public boolean isActive()     { return active; }
}
