package com.openpositioning.PositionMe.FusionFilter;

import com.google.android.gms.maps.model.LatLng;

public class Particle {
    public LatLng position;
    public double weight;

    public Particle(double latitude, double longitude, double weight) {
        this.position = new LatLng(latitude, longitude);
        this.weight = weight;
    }
}