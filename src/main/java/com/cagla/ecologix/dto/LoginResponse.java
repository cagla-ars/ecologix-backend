package com.cagla.ecologix.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private Long id;
    private String username;
    private String role;
    private Long courierId;

    private String branchName;
    private String branchCity;
    private String branchAddress;
    private Double branchLat;
    private Double branchLon;
}