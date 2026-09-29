package com.mednear.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FastApiMatchCandidateDto {

    private String name;
    
    @JsonProperty("genericName")
    private String genericName;
    
    private String strength;
    
    @JsonProperty("matchScore")
    private Double matchScore;

    public FastApiMatchCandidateDto() {}

    public FastApiMatchCandidateDto(String name, String genericName, String strength, Double matchScore) {
        this.name = name;
        this.genericName = genericName;
        this.strength = strength;
        this.matchScore = matchScore;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGenericName() { return genericName; }
    public void setGenericName(String genericName) { this.genericName = genericName; }

    public String getStrength() { return strength; }
    public void setStrength(String strength) { this.strength = strength; }

    public Double getMatchScore() { return matchScore; }
    public void setMatchScore(Double matchScore) { this.matchScore = matchScore; }
}
