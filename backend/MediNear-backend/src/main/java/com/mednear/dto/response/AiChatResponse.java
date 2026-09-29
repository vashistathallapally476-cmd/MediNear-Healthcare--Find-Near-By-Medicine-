package com.mednear.dto.response;

import com.mednear.dto.response.NearbyMedicineResponse;
import java.util.List;

/**
 * AI Assistant response.
 *
 * reply          → Claude's natural language answer
 * suggestedMeds  → medicine names Claude extracted from the user's query
 * nearbyStores   → populated if lat/lng was provided and Claude found relevant meds
 * intent         → what Claude understood: "SEARCH" | "INFO" | "GENERAL"
 */
public class AiChatResponse {

    private String                      reply;
    private List<String>                suggestedMedicines;
    private List<NearbyMedicineResponse> nearbyStores;
    private String                      intent;

    public String                       getReply()              { return reply; }
    public void                         setReply(String v)      { this.reply = v; }
    public List<String>                 getSuggestedMedicines() { return suggestedMedicines; }
    public void                         setSuggestedMedicines(List<String> v) { this.suggestedMedicines = v; }
    public List<NearbyMedicineResponse> getNearbyStores()       { return nearbyStores; }
    public void                         setNearbyStores(List<NearbyMedicineResponse> v) { this.nearbyStores = v; }
    public String                       getIntent()             { return intent; }
    public void                         setIntent(String v)     { this.intent = v; }
}
