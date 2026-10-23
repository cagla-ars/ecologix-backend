package com.cagla.ecologix.model;

import com.cagla.ecologix.model.enums.CourierStatus;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "couriers")
@Data 
public class Courier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String surname; // 🌟 Eklendi: Kurye Soyismi

    @Column(unique = true)
    private String plateNumber;
    
    private String vehicleType; // ELEKTRİK, DİZEL vb.
    
    @Enumerated(EnumType.STRING)
    private CourierStatus status;
    
    private Double carbonRatePerKm; 

    private Double currentLat;
    private Double currentLon;

    @Embedded
    private BranchInfo branchInfo;
}