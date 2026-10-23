package com.cagla.ecologix.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RoutingResult {
    private String algorithmName;
    private List<Shipment> route;
    private double totalDistance;
    private double totalCarbon;
    private long calculationTime;
    private double fitnessScore;
}