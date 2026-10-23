package com.cagla.ecologix.service;


import com.cagla.ecologix.model.*;
import com.cagla.ecologix.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class RoutingManager {

    @Autowired private List<RoutingAlgorithm> algorithms;
    @Autowired private ShipmentRepository shipmentRepository;
    @Autowired private OptimizationService optimizationService;

    // ÖRNEK DEĞER YERİNE: Ayarlar dosyasından gelen dinamik katsayılar
    @Value("${ecologix.weight.distance}")
    private double distanceWeight;

    @Value("${ecologix.weight.carbon}")
    private double carbonWeight;

    @Transactional
    public RoutingResult assignBestRoute(List<Shipment> shipments, Courier courier) {
        RoutingResult bestResult = null;
        double minFitness = Double.MAX_VALUE;

        for (RoutingAlgorithm algorithm : algorithms) {
            long startTime = System.currentTimeMillis();
            List<Shipment> route = algorithm.solve(shipments, courier.getCurrentLat(), courier.getCurrentLon());
            long duration = System.currentTimeMillis() - startTime;
            
            // Gerçek mesafe ve kuryenin veritabanındaki gerçek karbon oranı
            double distance = calculateTotalDistance(route, courier);
            double carbon = distance * courier.getCarbonRatePerKm();

            // DİNAMİK FITNESS HESABI: Kodda sayı yok, ayarlardan gelen katsayılar var
            double fitness = (distance * distanceWeight) + (carbon * carbonWeight);

            RoutingResult current = new RoutingResult(
                algorithm.getName(), route, distance, carbon, duration, fitness
            );

            if (fitness < minFitness) {
                minFitness = fitness;
                bestResult = current;
            }
        }

        if (bestResult != null) {
            updateShipmentsInDb(bestResult.getRoute(), courier.getId());
        }
        return bestResult;
    }

    private void updateShipmentsInDb(List<Shipment> route, Long courierId) {
        for (int i = 0; i < route.size(); i++) {
            Shipment s = route.get(i);
            s.setDeliveryIndex(i + 1);
            s.setCourierId(courierId);
            s.setStatus(com.cagla.ecologix.model.enums.ShipmentStatus.IN_TRANSIT);
            shipmentRepository.save(s);
        }
    }

    private double calculateTotalDistance(List<Shipment> route, Courier courier) {
        double total = 0;
        double lat = courier.getCurrentLat();
        double lon = courier.getCurrentLon();
        for (Shipment s : route) {
            total += optimizationService.calculateDistance(lat, lon, s.getLat(), s.getLon());
            lat = s.getLat(); lon = s.getLon();
        }
        return total;
    }
}