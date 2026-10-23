package com.cagla.ecologix.controller;

import com.cagla.ecologix.model.Courier;
import com.cagla.ecologix.model.RoutingResult;
import com.cagla.ecologix.model.Shipment;
import com.cagla.ecologix.service.CourierService;
import com.cagla.ecologix.service.RoutingManager;
import com.cagla.ecologix.service.ShipmentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/routing")
@CrossOrigin(origins = "*")
public class RoutingController {

    @Autowired
    private RoutingManager routingManager;

    @Autowired
    private CourierService courierService;

    @Autowired
    private ShipmentService shipmentService;

    @PostMapping("/assign-best/{courierId}")
    public RoutingResult startOptimization(@PathVariable Long courierId) {

        Courier courier = courierService.findById(courierId);

        List<Shipment> pendingShipments =
                shipmentService.findPendingShipmentsByBranch(
                        courier.getBranchInfo().getBranchCity()
                );

        return routingManager.assignBestRoute(
                pendingShipments,
                courier
        );
    }
}