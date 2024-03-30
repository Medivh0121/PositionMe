package com.openpositioning.PositionMe.sensors;

public class FusionLocation {
    private double latitude;
    private double longitude;
    private long timestamp;
    private String source; // "GNSS" or "PDR"

    // Constructor
    public FusionLocation(double latitude, double longitude, long timestamp, String source) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
        this.source = source;
    }

    // Getters
    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getSource() {
        return source;
    }

    // Setters
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public String toString() {
        return "FusionLocation{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", timestamp=" + timestamp +
                ", source='" + source + '\'' +
                '}';
    }
}
