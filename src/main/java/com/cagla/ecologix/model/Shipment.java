package com.cagla.ecologix.model;

import com.cagla.ecologix.model.enums.ShipmentStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "shipments")
@Data
public class Shipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String barcode; // ECO-20260001 formatı

    @Column(unique = true, nullable = false)
    private String trackingNumber; // Müşterinin sorgulayacağı numara

    private String senderName; // 🌟 Yeni: Gönderici İsmi
    private String receiverName; // Alıcı İsmi
    private String deliveryAddress; // Gidilecek Detaylı Adres
    
    private String originBranchName;
    private String originCity;

    private String destinationCity;

    private String currentHub;
    private String currentHubCity;

    private Double lat; // Kayseri şubesine gelince rota için şart
    private Double lon;

    private Double weight; 
    private String cargoType; 

    @Enumerated(EnumType.STRING)
    private ShipmentStatus status; 
    
    private Integer priority; 
    private Integer deliveryIndex; 
    private Long courierId; 

    private LocalDateTime createdAt; 
    private LocalDateTime estimatedDeliveryTime; 
    private LocalDateTime deliveredAt; 
    private String destinationBranchName;

    private String transferBranchName;
    private String transferCity;

    private Integer currentStep;
}