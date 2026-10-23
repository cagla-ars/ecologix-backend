package com.cagla.ecologix.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OptimizationService {
    private static final double EARTH_RADIUS = 6371; 

    @Value("${eco.emission.base-rate:0.15}")
    private double baseEmissionRate;

    @Value("${eco.emission.weight-factor:0.005}")
    private double weightEmissionFactor;

    @Value("${eco.optimization.carbon-weight:5.0}")
    private double carbonWeight;

    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return EARTH_RADIUS * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    public double calculateCarbonEmission(double distance, double currentWeight) {
        return distance * (baseEmissionRate + (currentWeight * weightEmissionFactor));
    }

    public double calculateEcoScore(double distance, double carbon) {
        return distance + (carbon * carbonWeight);
    }
}