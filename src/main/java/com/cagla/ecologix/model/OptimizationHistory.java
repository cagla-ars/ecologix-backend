package com.cagla.ecologix.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "optimization_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OptimizationHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String algorithmName;      
    private double totalDistance;      
    private double carbonEmitted;      
    private double carbonSaved;        
    private LocalDateTime timestamp;

    public OptimizationHistory(String algorithmName, double totalDistance, double carbonEmitted, double carbonSaved) {
        this.algorithmName = algorithmName;
        this.totalDistance = totalDistance;
        this.carbonEmitted = carbonEmitted;
        this.carbonSaved = carbonSaved;
        this.timestamp = LocalDateTime.now();
    }
}