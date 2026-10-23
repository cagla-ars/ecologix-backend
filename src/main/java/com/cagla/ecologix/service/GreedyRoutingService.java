package com.cagla.ecologix.service;

import com.cagla.ecologix.model.Shipment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GreedyRoutingService implements RoutingAlgorithm {

    @Autowired
    private OptimizationService optimizationService;

    @Override
    public String getName() {
        return "Greedy (En Yakın Komşu)";
    }

    @Override
    public List<Shipment> solve(List<Shipment> shipments, double startLat, double startLon) {
        if (shipments == null || shipments.isEmpty()) {
            return new ArrayList<>();
        }
        List<Shipment> unvisited = new ArrayList<>(shipments);
        List<Shipment> route = new ArrayList<>();
        
        double currentLat = startLat;
        double currentLon = startLon;

        while (!unvisited.isEmpty()) {
            Shipment nearest = null;
            double minDistance = Double.MAX_VALUE;

            for (Shipment s : unvisited) {
                double dist = optimizationService.calculateDistance(currentLat, currentLon, s.getLat(), s.getLon());
                if (dist < minDistance) {
                    minDistance = dist;
                    nearest = s;
                }
            }

            if (nearest != null) {
                route.add(nearest);
                unvisited.remove(nearest);
                currentLat = nearest.getLat();
                currentLon = nearest.getLon();
            }
        }
        return route;
    }
}