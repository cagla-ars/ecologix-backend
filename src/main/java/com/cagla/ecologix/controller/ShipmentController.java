package com.cagla.ecologix.controller;

import com.cagla.ecologix.model.Shipment;
import com.cagla.ecologix.model.enums.ShipmentStatus;
import com.cagla.ecologix.model.Courier;
import com.cagla.ecologix.service.ShipmentService;
import com.cagla.ecologix.service.CourierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/shipments")
@CrossOrigin(origins = "*")
public class ShipmentController {

    @Autowired
    private ShipmentService shipmentService;

    @Autowired
    private CourierService courierService;

    @GetMapping
    public List<Shipment> getAll() {
        return shipmentService.getAllShipments();
    }

    @PostMapping
    public Shipment create(@RequestBody Shipment shipment) {
        return shipmentService.saveShipment(shipment);
    }

    @GetMapping("/track/{trackingNumber}")
    public ResponseEntity<?> trackByCustomer(@PathVariable String trackingNumber) {
        try {
            Shipment s = shipmentService.findByTrackingNumber(trackingNumber);

            Map<String, Object> response = new HashMap<>();
            response.put("id", s.getId());
            response.put("barcode", s.getBarcode());
            response.put("trackingNumber", s.getTrackingNumber());
            response.put("receiverName", s.getReceiverName());
            response.put("senderName", s.getSenderName());
            response.put("deliveryAddress", s.getDeliveryAddress());
            response.put("status", s.getStatus().toString());

            response.put("originBranchName", s.getOriginBranchName());
            response.put("originCity", s.getOriginCity());

            response.put("destinationBranchName", s.getDestinationBranchName());
            response.put("destinationCity", s.getDestinationCity());

            response.put("transferBranchName", s.getTransferBranchName());
            response.put("transferCity", s.getTransferCity());

            response.put("currentHub", s.getCurrentHub());
            response.put("currentHubCity", s.getCurrentHubCity());
            response.put("currentStep", s.getCurrentStep());

            if (s.getStatus().toString().equals("IN_TRANSIT") && s.getCourierId() != null) {
                Courier c = courierService.findById(s.getCourierId());
                if (c != null) {
                    response.put("courierName", c.getName() + " " + c.getSurname());
                    response.put("courierLat", c.getCurrentLat());
                    response.put("courierLon", c.getCurrentLon());
                    response.put("plateNumber", c.getPlateNumber());
                }
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/assign-route")
    public String assignRoute(
            @RequestBody List<Long> shipmentIds,
            @RequestParam Long courierId,
            @RequestParam String algorithm,
            @RequestParam double distance,
            @RequestParam double carbonEmitted,
            @RequestParam double carbonSaved) {

        shipmentService.assignOrder(shipmentIds, courierId, algorithm, distance, carbonEmitted, carbonSaved);
        return "Rota atandı ve tasarruf verileri OptimizationHistory'e kaydedildi!";
    }

    @PostMapping("/{id}/deliver")
    public String deliverShipment(@PathVariable Long id) {
        shipmentService.deliverShipment(id);
        return "Paket başarıyla teslim edildi ve zaman damgası vuruldu.";
    }

    @GetMapping("/courier/{courierId}/active-route")
    public List<Shipment> getActiveRoute(@PathVariable Long courierId) {
        return shipmentService.getActiveRouteForCourier(courierId);
    }

    @GetMapping("/branch")
    public List<Shipment> getShipmentsByBranch(@RequestParam String currentHub) {
        return shipmentService.getShipmentsByCurrentHub(currentHub);
    }

    @PostMapping("/{id}/forward")
    public String forwardShipment(@PathVariable Long id) {
        shipmentService.forwardShipment(id);
        return "Paket güzergah üzerindeki sonraki merkeze aktarıldı.";
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestParam ShipmentStatus status
    ) {
        shipmentService.updateStatus(id, status);
        return ResponseEntity.ok("Durum güncellendi.");
    }

    @PostMapping("/{id}/ready-for-delivery")
    public String markReadyForDelivery(@PathVariable Long id) {
        shipmentService.updateStatus(id, ShipmentStatus.PENDING);
        return "Paket dağıtıma hazır hale getirildi.";
    }
    
    @GetMapping("/courier/{courierId}/today-delivered-count")
    public long getTodayDeliveredCount(@PathVariable Long courierId) {
        return shipmentService.getTodayDeliveredCountForCourier(courierId);
    }
}
