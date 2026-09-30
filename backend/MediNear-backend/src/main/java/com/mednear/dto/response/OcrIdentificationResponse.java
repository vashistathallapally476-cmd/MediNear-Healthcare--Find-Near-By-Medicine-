package com.mednear.dto.response;

import java.util.List;

public class OcrIdentificationResponse {

    private List<MedicineMatch> matches;

    public OcrIdentificationResponse() {
    }

    public OcrIdentificationResponse(List<MedicineMatch> matches) {
        this.matches = matches;
    }

    public List<MedicineMatch> getMatches() {
        return matches;
    }

    public void setMatches(List<MedicineMatch> matches) {
        this.matches = matches;
    }

    public static class MedicineMatch {

        private String medicine_name;
        private double confidence;

        public MedicineMatch() {
        }

        public MedicineMatch(String medicine_name, double confidence) {
            this.medicine_name = medicine_name;
            this.confidence = confidence;
        }

        public String getMedicine_name() {
            return medicine_name;
        }

        public void setMedicine_name(String medicine_name) {
            this.medicine_name = medicine_name;
        }

        public double getConfidence() {
            return confidence;
        }

        public void setConfidence(double confidence) {
            this.confidence = confidence;
        }
    }
}