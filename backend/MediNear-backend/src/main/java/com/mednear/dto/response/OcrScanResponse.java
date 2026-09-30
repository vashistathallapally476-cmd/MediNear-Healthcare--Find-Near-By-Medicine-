package com.mednear.dto.response;

import java.util.ArrayList;
import java.util.List;

public class OcrScanResponse {

    private Long scanId;
    private String imageUrl;
    private String rawText;
    private String status;
    private List<OcrCandidateResponseDto> candidates = new ArrayList<>();

    public OcrScanResponse() {}

    public OcrScanResponse(Long scanId, String imageUrl, String rawText, String status, List<OcrCandidateResponseDto> candidates) {
        this.scanId = scanId;
        this.imageUrl = imageUrl;
        this.rawText = rawText;
        this.status = status;
        this.candidates = candidates;
    }

    public Long getScanId() { return scanId; }
    public void setScanId(Long scanId) { this.scanId = scanId; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getRawText() { return rawText; }
    public void setRawText(String rawText) { this.rawText = rawText; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<OcrCandidateResponseDto> getCandidates() { return candidates; }
    public void setCandidates(List<OcrCandidateResponseDto> candidates) { this.candidates = candidates; }
}
