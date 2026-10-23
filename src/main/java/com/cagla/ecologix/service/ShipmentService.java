package com.cagla.ecologix.service;

import com.cagla.ecologix.model.Courier;
import com.cagla.ecologix.model.OptimizationHistory;
import com.cagla.ecologix.model.Shipment;
import com.cagla.ecologix.model.enums.CourierStatus;
import com.cagla.ecologix.model.enums.ShipmentStatus;
import com.cagla.ecologix.repository.CourierRepository;
import com.cagla.ecologix.repository.OptimizationHistoryRepository;
import com.cagla.ecologix.repository.ShipmentRepository;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ShipmentService {

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private OptimizationHistoryRepository historyRepository;

    @Autowired
    private CourierRepository courierRepository;

    public List<Shipment> getAllShipments() {
        return shipmentRepository.findAll();
    }
 
    public List<Shipment> getShipmentsByCurrentHub(String currentHub) {
        return shipmentRepository.findAll()
                .stream()
                .filter(s -> s.getCurrentHub() != null && s.getCurrentHub().equalsIgnoreCase(currentHub))
                .toList();
    }

    public List<Shipment> findPendingShipmentsByBranch(String branchCity) {
        return shipmentRepository.findAll()
                .stream()
                .filter(s ->
                        s.getStatus() == ShipmentStatus.PENDING &&
                        s.getCurrentHubCity() != null &&
                        s.getCurrentHubCity().equalsIgnoreCase(branchCity)
                )
                .filter(s ->
                        s.getDestinationCity() != null &&
                        s.getDestinationCity().equalsIgnoreCase(branchCity)
                )
                .filter(s -> s.getLat() != null && s.getLon() != null)
                .toList();
    }

    public Shipment saveShipment(Shipment shipment) {
        prepareBasicShipmentFields(shipment);
        prepareRouteFields(shipment);

        return shipmentRepository.save(shipment);
    }

    private void prepareBasicShipmentFields(Shipment shipment) {
        if (shipment.getStatus() == null) {
            shipment.setStatus(ShipmentStatus.AT_BRANCH);
        }

        if (shipment.getBarcode() == null || shipment.getBarcode().isEmpty()) {
            shipment.setBarcode("ECO-" + System.currentTimeMillis() % 100000);
        }

        if (shipment.getTrackingNumber() == null || shipment.getTrackingNumber().isEmpty()) {
            shipment.setTrackingNumber("TRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }

        if (shipment.getCreatedAt() == null) {
            shipment.setCreatedAt(java.time.LocalDateTime.now());
        }

        if (shipment.getOriginBranchName() == null || shipment.getOriginBranchName().isEmpty()) {
            shipment.setOriginBranchName(shipment.getCurrentHub());
        }

        if ((shipment.getCurrentHubCity() == null || shipment.getCurrentHubCity().isEmpty())
                && shipment.getOriginCity() != null) {
            shipment.setCurrentHubCity(shipment.getOriginCity());
        }
    }

    private void prepareRouteFields(Shipment shipment) {
        if (shipment.getCurrentStep() == null) {
            shipment.setCurrentStep(0);
        }

        if (shipment.getCurrentHub() == null || shipment.getCurrentHub().isEmpty()) {
            shipment.setCurrentHub(shipment.getOriginBranchName());
        }

        if (shipment.getCurrentHubCity() == null || shipment.getCurrentHubCity().isEmpty()) {
            shipment.setCurrentHubCity(shipment.getOriginCity());
        }

        if (shipment.getDestinationBranchName() == null || shipment.getDestinationBranchName().isEmpty()) {
            shipment.setDestinationBranchName(shipment.getDestinationCity());
        }

        if (shipment.getTransferBranchName() != null && shipment.getTransferBranchName().isBlank()) {
            shipment.setTransferBranchName(null);
        }

        if (shipment.getTransferCity() != null && shipment.getTransferCity().isBlank()) {
            shipment.setTransferCity(null);
        }
    }

    public Shipment findByTrackingNumber(String trackingNumber) {
        return shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new RuntimeException("Kargo takip numarası bulunamadı!"));
    }

    @Transactional
    public void assignOrder(
            List<Long> shipmentIds,
            Long courierId,
            String algorithm,
            double distance,
            double emitted,
            double saved
    ) {
        for (int i = 0; i < shipmentIds.size(); i++) {
            Long currentId = shipmentIds.get(i);
            int orderIndex = i + 1;

            Shipment s = shipmentRepository.findById(currentId)
                    .orElseThrow(() -> new RuntimeException("Paket bulunamadı: " + currentId));

            s.setStatus(ShipmentStatus.IN_TRANSIT);
            s.setCourierId(courierId);
            s.setDeliveryIndex(orderIndex);

            shipmentRepository.save(s);
        }

        Courier courier = courierRepository.findById(courierId)
                .orElseThrow(() -> new RuntimeException("Kurye bulunamadı! ID: " + courierId));

        courier.setStatus(CourierStatus.ON_DELIVERY);
        courierRepository.save(courier);

        OptimizationHistory history = new OptimizationHistory(algorithm, distance, emitted, saved);
        historyRepository.save(history);
    }

    public void forwardShipment(Long id) {
        Shipment s = shipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paket bulunamadı!"));

        boolean hasTransfer =
                s.getTransferBranchName() != null &&
                !s.getTransferBranchName().isBlank();

  
        if (hasTransfer && (s.getCurrentStep() == null || s.getCurrentStep() == 0)) {
            s.setCurrentStep(1);
            s.setCurrentHub(s.getTransferBranchName());
            s.setCurrentHubCity(s.getTransferCity());
            s.setStatus(ShipmentStatus.AT_BRANCH);
        } else {
            s.setCurrentStep(hasTransfer ? 2 : 1);
            s.setCurrentHub(s.getDestinationBranchName());
            s.setCurrentHubCity(s.getDestinationCity());
            s.setStatus(ShipmentStatus.AT_BRANCH);
        }

        shipmentRepository.save(s);
    }

    public void deliverShipment(Long id) {
        Shipment s = shipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paket bulunamadı!"));

        s.setStatus(ShipmentStatus.DELIVERED);
        s.setDeliveredAt(java.time.LocalDateTime.now());
        shipmentRepository.save(s);
    }

    public List<Shipment> getActiveRouteForCourier(Long courierId) {
        return shipmentRepository.findByCourierIdAndStatusOrderByDeliveryIndexAsc(
                courierId,
                ShipmentStatus.IN_TRANSIT
        );
    }

    public void updateStatus(Long id, ShipmentStatus status) {
        Shipment s = shipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paket bulunamadı!"));

        s.setStatus(status);
        shipmentRepository.save(s);
    }

    public long getTodayDeliveredCountForCourier(Long courierId) {
        LocalDate today = LocalDate.now();

        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        return shipmentRepository.countByCourierIdAndStatusAndDeliveredAtBetween(
                courierId,
                ShipmentStatus.DELIVERED,
                start,
                end
        );
    }

}
