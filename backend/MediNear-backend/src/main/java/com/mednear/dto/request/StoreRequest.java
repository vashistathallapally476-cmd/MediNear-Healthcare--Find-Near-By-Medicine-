package com.mednear.dto.request;

import jakarta.validation.constraints.*;

public class StoreRequest {

    @NotBlank(message = "Store name is required")
    @Size(max = 200)
    private String storeName;

    @NotBlank(message = "Address is required")
    @Size(max = 500)
    private String address;

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0",  message = "Latitude out of range")
    @DecimalMax(value = "90.0",   message = "Latitude out of range")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude out of range")
    @DecimalMax(value = "180.0",  message = "Longitude out of range")
    private Double longitude;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[+]?[0-9 \\-]{7,20}$", message = "Invalid phone number")
    private String phone;

    public String getStoreName()          { return storeName; }
    public void   setStoreName(String v)  { this.storeName = v; }
    public String getAddress()            { return address; }
    public void   setAddress(String v)    { this.address = v; }
    public Double getLatitude()           { return latitude; }
    public void   setLatitude(Double v)   { this.latitude = v; }
    public Double getLongitude()          { return longitude; }
    public void   setLongitude(Double v)  { this.longitude = v; }
    public String getPhone()              { return phone; }
    public void   setPhone(String v)      { this.phone = v; }
}
