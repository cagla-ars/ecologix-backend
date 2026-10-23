package com.cagla.ecologix.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "branches")
@Data
public class Branch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String branchName;

    @Column(nullable = false)
    private String branchCity;

    @Column(nullable = false)
    private String branchAddress;

    @Column(nullable = false)
    private Double branchLat;

    @Column(nullable = false)
    private Double branchLon;
}
