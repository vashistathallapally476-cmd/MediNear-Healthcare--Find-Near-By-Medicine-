package com.mednear.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FastApiOcrResponse {

    private boolean success = true;

    @JsonProperty("ocrText")
    private String ocrText;

    private List<FastApiMatchCandidateDto> matches = new ArrayList<>();

    private String error;

    public FastApiOcrResponse() {}

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getOcrText() { return ocrText; }
    public void setOcrText(String ocrText) { this.ocrText = ocrText; }

    public List<FastApiMatchCandidateDto> getMatches() { return matches; }
    public void setMatches(List<FastApiMatchCandidateDto> matches) { this.matches = matches; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}
