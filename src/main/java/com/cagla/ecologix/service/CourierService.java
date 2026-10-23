package com.cagla.ecologix.service;

import com.cagla.ecologix.model.Courier;
import com.cagla.ecologix.model.enums.CourierStatus;
import com.cagla.ecologix.repository.CourierRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CourierService {
    
    @Autowired 
    private CourierRepository courierRepository;
    
    public Courier findById(Long id) {
        return courierRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Kurye bulunamadı! ID: " + id));
    }

    public List<Courier> getAll() {
        return courierRepository.findAll();
    }

    public Courier save(Courier courier) {

        courier.setStatus(CourierStatus.AVAILABLE);

        courier.setCarbonRatePerKm(
            calculateCarbonRate(courier.getVehicleType())
        );

        // Şube konumunu başlangıç konumu yap
        if (courier.getBranchInfo() != null) {

            courier.setCurrentLat(
                courier.getBranchInfo().getBranchLat()
            );

            courier.setCurrentLon(
                courier.getBranchInfo().getBranchLon()
            );
        }

        return courierRepository.save(courier);
    }

    public Courier updateVehicle(Long id, String type) {

        Courier courier = courierRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Kurye bulunamadı! ID: " + id));

        courier.setVehicleType(type);

        courier.setCarbonRatePerKm(
            calculateCarbonRate(type)
        );

        return courierRepository.save(courier);
    }

    private Double calculateCarbonRate(String vehicleType) {
        if (vehicleType == null) {
            return 0.15;
        }

        switch (vehicleType.toUpperCase()) {
            case "ELECTRIC":
                return 0.02;

            case "MOTORCYCLE":
                return 0.08;

            case "HYBRID":
                return 0.12;

            case "GASOLINE":
                return 0.19;

            case "DIESEL":
                return 0.24;

            default:
                return 0.15;
        }
    }
    
    public Courier updateLocation(Long id, Double lat, Double lon) {
        Courier courier = courierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kurye bulunamadı! ID: " + id));

        courier.setCurrentLat(lat);
        courier.setCurrentLon(lon);

        return courierRepository.save(courier);
    }

    public Courier updateStatus(Long id, String status) {
        Courier courier = courierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kurye bulunamadı! ID: " + id));
        
        try {
            courier.setStatus(CourierStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            courier.setStatus(CourierStatus.ON_DELIVERY);
        }

        return courierRepository.save(courier);
    }
}