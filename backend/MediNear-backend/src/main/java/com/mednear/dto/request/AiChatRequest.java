package com.mednear.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * User message sent to the AI Medicine Assistant.
 *
 * latitude + longitude are OPTIONAL.
 * If provided, the AI can also suggest nearby pharmacies.
 * If not provided, the AI gives general medicine advice only.
 */
public class AiChatRequest {

    @NotBlank(message = "Message is required")
    @Size(max = 1000, message = "Message too long (max 1000 characters)")
    private String message;

    @DecimalMin(value = "-90.0",  message = "Invalid latitude")
    @DecimalMax(value = "90.0",   message = "Invalid latitude")
    private Double latitude;

    @DecimalMin(value = "-180.0", message = "Invalid longitude")
    @DecimalMax(value = "180.0",  message = "Invalid longitude")
    private Double longitude;

    // Optional: include conversation history for multi-turn chat
    private String conversationHistory;

    public String getMessage()                        { return message; }
    public void   setMessage(String v)                { this.message = v; }
    public Double getLatitude()                       { return latitude; }
    public void   setLatitude(Double v)               { this.latitude = v; }
    public Double getLongitude()                      { return longitude; }
    public void   setLongitude(Double v)              { this.longitude = v; }
    public String getConversationHistory()            { return conversationHistory; }
    public void   setConversationHistory(String v)    { this.conversationHistory = v; }
}
