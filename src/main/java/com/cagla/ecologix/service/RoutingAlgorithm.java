package com.cagla.ecologix.service;

import com.cagla.ecologix.model.Shipment;
import java.util.List;

public interface RoutingAlgorithm {

    List<Shipment> solve(
            List<Shipment> shipments,
            double startLat,
            double startLon
    );

    String getName();
}