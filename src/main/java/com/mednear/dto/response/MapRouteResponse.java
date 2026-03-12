package com.mednear.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/**
 * Response for GET /api/maps/route
 * Contains everything the frontend needs to draw a route on Google Maps:
 *   - encodedPolyline  → pass to google.maps.geometry.encoding.decodePath()
 *   - distanceText     → "2.4 km"
 *   - durationText     → "7 mins"
 *   - steps            → turn-by-turn navigation instructions
 *   - mapsUrl          → deeplink to open Google Maps app / web
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MapRouteResponse {

    private String       encodedPolyline;   // for drawing on map
    private String       distanceText;      // "2.4 km"
    private int          distanceMeters;
    private String       durationText;      // "7 mins"
    private int          durationSeconds;
    private String       startAddress;
    private String       endAddress;
    private List<Step>   steps;             // turn-by-turn
    private String       mapsUrl;           // open in Google Maps app

    // ── Step (one navigation instruction) ───────────────────────
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Step {
        private String instruction;   // "Turn left onto MG Road"
        private String distance;      // "300 m"
        private String duration;      // "1 min"
        private double startLat;
        private double startLng;
        private double endLat;
        private double endLng;

        public String getInstruction() { return instruction; }
        public void   setInstruction(String v) { this.instruction = v; }
        public String getDistance()    { return distance; }
        public void   setDistance(String v)    { this.distance = v; }
        public String getDuration()    { return duration; }
        public void   setDuration(String v)    { this.duration = v; }
        public double getStartLat()    { return startLat; }
        public void   setStartLat(double v)    { this.startLat = v; }
        public double getStartLng()    { return startLng; }
        public void   setStartLng(double v)    { this.startLng = v; }
        public double getEndLat()      { return endLat; }
        public void   setEndLat(double v)      { this.endLat = v; }
        public double getEndLng()      { return endLng; }
        public void   setEndLng(double v)      { this.endLng = v; }
    }

    public String       getEncodedPolyline() { return encodedPolyline; }
    public void         setEncodedPolyline(String v) { this.encodedPolyline = v; }
    public String       getDistanceText()    { return distanceText; }
    public void         setDistanceText(String v)    { this.distanceText = v; }
    public int          getDistanceMeters()  { return distanceMeters; }
    public void         setDistanceMeters(int v)     { this.distanceMeters = v; }
    public String       getDurationText()    { return durationText; }
    public void         setDurationText(String v)    { this.durationText = v; }
    public int          getDurationSeconds() { return durationSeconds; }
    public void         setDurationSeconds(int v)    { this.durationSeconds = v; }
    public String       getStartAddress()    { return startAddress; }
    public void         setStartAddress(String v)    { this.startAddress = v; }
    public String       getEndAddress()      { return endAddress; }
    public void         setEndAddress(String v)      { this.endAddress = v; }
    public List<Step>   getSteps()           { return steps; }
    public void         setSteps(List<Step> v)       { this.steps = v; }
    public String       getMapsUrl()         { return mapsUrl; }
    public void         setMapsUrl(String v)         { this.mapsUrl = v; }
}
