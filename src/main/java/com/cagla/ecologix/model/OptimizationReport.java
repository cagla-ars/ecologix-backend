package com.cagla.ecologix.model;

import java.util.List;
import lombok.Data;

@Data
public class OptimizationReport {
    private String algorithmName;
    private double totalDistance;
    private double totalCarbon;
    private long executionTime; // Hesaplama süresi (ms)
    private List<Shipment> route;

    public OptimizationReport(String algorithmName, double totalDistance, double totalCarbon, long executionTime, List<Shipment> route) {
        this.algorithmName = algorithmName;
        this.totalDistance = totalDistance;
        this.totalCarbon = totalCarbon;
        this.executionTime = executionTime;
        this.route = route;
    }
}