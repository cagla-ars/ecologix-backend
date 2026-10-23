package com.cagla.ecologix.model;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class BranchInfo {
    private String branchName;
    private String branchCity;
    private String branchAddress;
    private Double branchLat;
    private Double branchLon;
}
