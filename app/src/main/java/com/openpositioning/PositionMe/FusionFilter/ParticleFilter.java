package com.openpositioning.PositionMe.FusionFilter;
import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ParticleFilter {
    private List<Particle> particles;
    private Random random = new Random();
    private static final int NUM_PARTICLES = 1000;
    private double wifiNoise = 0.0000035; // Adjust this value based on your sensor's accuracy.
    private double gnssNoise = 0.0000095;
    private double pdrNoise = 0.0000095;

    public ParticleFilter(LatLng initialPosition) {
        this.particles = new ArrayList<>();
        // Initialize particles around the provided initial position
        for (int i = 0; i < NUM_PARTICLES; i++) {
            double lat = initialPosition.latitude + (random.nextDouble() - 0.5) * 0.0001; // Adjust the range as needed
            double lon = initialPosition.longitude + (random.nextDouble() - 0.5) * 0.0001;
            particles.add(new Particle(lat, lon, 1.0 / NUM_PARTICLES));
        }
    }

//    public LatLng particleFilter(LatLng gnssCoord, LatLng pdrCoord) {
//        // Apply the motion model
//        for (Particle particle : particles) {
//            particle.position = new LatLng(
//                    particle.position.latitude + (random.nextDouble() - 0.5) * 0.00001, // Simulate slight random movement
//                    particle.position.longitude + (random.nextDouble() - 0.5) * 0.00001);
//        }
//
//        // Update weights based on GNSS and PDR measurements
//        if (gnssCoord != null) {
//            updateWeights(gnssCoord, sensorNoise);
//        }
//        if (pdrCoord != null) {
//            updateWeights(pdrCoord, sensorNoise);
//        }
//
//        normalizeWeights();
//        resample();
//
//        return estimatePosition();
//    }

    public LatLng particleFilter(LatLng wifiCoord, LatLng gnssCoord, LatLng pdrCoord) {
        // Apply the motion model
        for (Particle particle : particles) {
            particle.position = new LatLng(
                    particle.position.latitude + (random.nextDouble() - 0.5) * 0.00001, // Simulate slight random movement
                    particle.position.longitude + (random.nextDouble() - 0.5) * 0.00001);
        }

        if (wifiCoord != null && isValidCoordinate(wifiCoord)) {
            updateWeights(wifiCoord, wifiNoise);
        }
        // Update weights based on GNSS and PDR measurements
        if (gnssCoord != null && isValidCoordinate(gnssCoord)) {
            updateWeights(gnssCoord, gnssNoise);
        }
        if (pdrCoord != null && isValidCoordinate(pdrCoord)) {
            updateWeights(pdrCoord, pdrNoise);
        }

        normalizeWeights();
        resample();

        return estimatePosition();
    }

    private boolean isValidCoordinate(LatLng coord) {
        // Check if the coordinate is not NaN, not exactly (0,0), and within reasonable bounds
        return !Double.isNaN(coord.latitude) && !Double.isNaN(coord.longitude)
                && !(coord.latitude == 0.0 && coord.longitude == 0.0) // Exclude (0,0) as invalid
                && coord.latitude >= -90 && coord.latitude <= 90
                && coord.longitude >= -180 && coord.longitude <= 180;
    }

    private void updateWeights(LatLng coord, double noise) {
        for (Particle particle : particles) {
            double distance = Math.sqrt(
                    Math.pow(particle.position.latitude - coord.latitude, 2) +
                            Math.pow(particle.position.longitude - coord.longitude, 2));
            particle.weight *= Math.exp(-Math.pow(distance, 2) / (2 * Math.pow(noise, 2)));
        }
    }


    // Old version
//    private void normalizeWeights() {
//        double totalWeight = 0.0;
//        for (Particle particle : particles) {
//            totalWeight += particle.weight;
//        }
//        for (Particle particle : particles) {
//            particle.weight /= totalWeight;
//        }
//    }

    //Not divided by 0
    private void normalizeWeights() {
        double totalWeight = 0.0;
        for (Particle particle : particles) {
            totalWeight += particle.weight;
        }
        if (totalWeight == 0) {
            // If total weight is zero, assign equal weights to avoid division by zero
            double equalWeight = 1.0 / particles.size();
            for (Particle particle : particles) {
                particle.weight = equalWeight;
            }
        } else {
            for (Particle particle : particles) {
                particle.weight /= totalWeight;
            }
        }
    }




//    private void resample() {
//
//        List<Particle> newParticles = new ArrayList<>();
//        double[] cumulativeSum = new double[particles.size()];
//        cumulativeSum[0] = particles.get(0).weight;
//        for (int i = 1; i < particles.size(); i++) {
//            cumulativeSum[i] = cumulativeSum[i - 1] + particles.get(i).weight;
//        }
//
//        for (int i = 0; i < NUM_PARTICLES; i++) {
//            double randomVal = random.nextDouble();
//            for (int j = 0; j < cumulativeSum.length; j++) {
//                if (cumulativeSum[j] >= randomVal) {
//                    Particle p = particles.get(j);
//                    newParticles.add(new Particle(p.position.latitude, p.position.longitude, 1.0 / NUM_PARTICLES));
//                    break;
//                }
//            }
//        }
//        particles = newParticles;
//    }

    private void resample() {
        if (particles.isEmpty()) {
            return; // Early return if there are no particles to resample
        }

        List<Particle> newParticles = new ArrayList<>();
        double[] cumulativeSum = new double[particles.size()];
        cumulativeSum[0] = particles.get(0).weight;
        for (int i = 1; i < particles.size(); i++) {
            cumulativeSum[i] = cumulativeSum[i - 1] + particles.get(i).weight;
        }

        // Ensure we are handling the case where all weights might be zero
        if (cumulativeSum[cumulativeSum.length - 1] == 0) {
            // Optional: Re-initialize particles here if needed
            // For now, we'll simply return to avoid the IndexOutOfBoundsException
            return;
        }

        for (int i = 0; i < NUM_PARTICLES; i++) {
            double randomVal = random.nextDouble() * cumulativeSum[cumulativeSum.length - 1]; // Scale by the total weight
            for (int j = 0; j < cumulativeSum.length; j++) {
                if (cumulativeSum[j] >= randomVal) {
                    Particle p = particles.get(j);
                    newParticles.add(new Particle(p.position.latitude, p.position.longitude, 1.0 / NUM_PARTICLES));
                    break;
                }
            }
        }
        particles = newParticles;
    }

//    private LatLng estimatePosition() {
//        double sumLat = 0.0, sumLon = 0.0, totalWeight = 0.0;
//        for (Particle particle : particles) {
//            sumLat += particle.position.latitude * particle.weight;
//            sumLon += particle.position.longitude * particle.weight;
//            totalWeight += particle.weight;
//        }
//
//        // Make sure to divide by the total weight to get the weighted average
//        double estimatedLat = sumLat / totalWeight;
//        double estimatedLon = sumLon / totalWeight;
//        return new LatLng(estimatedLat, estimatedLon);
//    }

    private LatLng lastKnownValidPosition = null; // Initialize this variable appropriately

    private LatLng estimatePosition() {
        double sumLat = 0.0, sumLon = 0.0, totalWeight = 0.0;
        for (Particle particle : particles) {
            sumLat += particle.position.latitude * particle.weight;
            sumLon += particle.position.longitude * particle.weight;
            totalWeight += particle.weight;
        }

        if (totalWeight == 0) {
            // Return the last known valid position if total weight is 0
            return lastKnownValidPosition != null ? lastKnownValidPosition : new LatLng(0.0, 0.0); // Fallback to (0,0) if no last known position
        }

        double estimatedLat = sumLat / totalWeight;
        double estimatedLon = sumLon / totalWeight;
        lastKnownValidPosition = new LatLng(estimatedLat, estimatedLon); // Update the last known valid position
        return lastKnownValidPosition;
    }
}
