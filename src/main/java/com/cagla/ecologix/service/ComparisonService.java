package com.cagla.ecologix.service;

import com.cagla.ecologix.model.*;
import com.cagla.ecologix.model.enums.ShipmentStatus;
import com.cagla.ecologix.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ComparisonService {

    // Spring, RoutingAlgorithm'i implement eden tüm servisleri buraya otomatik toplar
    @Autowired 
    private List<RoutingAlgorithm> algorithms;

    @Autowired private OptimizationService optimizationService;
    @Autowired private CourierRepository courierRepository;
    @Autowired private ShipmentRepository shipmentRepository;
    @Autowired private OptimizationHistoryRepository historyRepository;

    // Özel istatistik hesaplamaları için tekil enjeksiyonlar
    @Autowired private GreedyRoutingService greedyService;
    @Autowired private AntColonyOptimizationService acoService;

    public List<OptimizationReport> compareAll(Long courierId, String branchCity) {
        Courier courier = courierRepository.findById(courierId)
                .orElseThrow(() -> new RuntimeException("Kurye bulunamadı!"));

        List<Shipment> pendingShipments = shipmentRepository.findAll()
                .stream()
                .filter(s -> s.getStatus() == ShipmentStatus.PENDING)
                .filter(s -> s.getCurrentHubCity() != null && s.getCurrentHubCity().equalsIgnoreCase(branchCity))
                .filter(s -> s.getDestinationCity() != null && s.getDestinationCity().equalsIgnoreCase(branchCity))
                .filter(s -> s.getLat() != null && s.getLon() != null)
                .toList();

        return compareAlgorithms(pendingShipments, courier);
    }

   public List<OptimizationReport> compareAlgorithms(List<Shipment> shipments, Courier courier) {
        if (shipments == null || shipments.isEmpty()) {
            return new ArrayList<>();
        }

        List<OptimizationReport> reports = new ArrayList<>();

        for (RoutingAlgorithm algorithm : algorithms) {
            long startTime = System.currentTimeMillis();

            List<Shipment> route = algorithm.solve(
                shipments,
                courier.getCurrentLat(),
                courier.getCurrentLon()
            );

            long duration = System.currentTimeMillis() - startTime;

            reports.add(createReport(algorithm.getName(), route, courier, duration));
        }

        return reports;
    }

    private OptimizationReport createReport(String name, List<Shipment> route, Courier courier, long time) {
        double dist = calculateRouteTotalDistance(route, courier);
        double carbon = dist * courier.getCarbonRatePerKm();
        return new OptimizationReport(name, dist, carbon, time, route);
    }

    public double calculateRouteTotalDistance(List<Shipment> route, Courier courier) {
        double total = 0;
        double curLat = courier.getCurrentLat();
        double curLon = courier.getCurrentLon();

        for (Shipment s : route) {
            total += optimizationService.calculateDistance(curLat, curLon, s.getLat(), s.getLon());
            curLat = s.getLat();
            curLon = s.getLon();
        }
        return total;
    }

    public Map<String, Object> getEfficiencyStats(List<Shipment> shipments, Courier courier) {
        List<Shipment> greedyRoute = greedyService.solve(shipments, courier.getCurrentLat(), courier.getCurrentLon());
        double greedyDist = calculateRouteTotalDistance(greedyRoute, courier);

        List<Shipment> acoRoute = acoService.solve(shipments, courier.getCurrentLat(), courier.getCurrentLon());
        double acoDist = calculateRouteTotalDistance(acoRoute, courier);

        double distanceSaved = Math.max(0, greedyDist - acoDist);
        double carbonSaved = distanceSaved * courier.getCarbonRatePerKm();

        Map<String, Object> results = new HashMap<>();
        results.put("distanceSaved", distanceSaved);
        results.put("carbonSaved", carbonSaved);
        return results;
    }

    public void saveWinningRoute(String algorithmName, double distance, double carbonEmitted, double carbonSaved) {
        OptimizationHistory history = new OptimizationHistory();
        history.setAlgorithmName(algorithmName);
        history.setTotalDistance(distance);
        history.setCarbonEmitted(carbonEmitted);
        history.setCarbonSaved(carbonSaved);
        history.setTimestamp(LocalDateTime.now());
        
        historyRepository.save(history);
    }
}