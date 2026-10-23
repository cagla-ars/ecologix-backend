package com.cagla.ecologix.dto;

import com.cagla.ecologix.model.enums.UserRole;
import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private UserRole role;

    private Long courierId;

    private String branchName;
    private String branchCity;
    private String branchAddress;
    private Double branchLat;
    private Double branchLon;
}
