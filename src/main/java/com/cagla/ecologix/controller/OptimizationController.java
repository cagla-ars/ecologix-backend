package com.cagla.ecologix.controller;

import com.cagla.ecologix.model.OptimizationReport;
import com.cagla.ecologix.service.ComparisonService;
import com.cagla.ecologix.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/optimization")
@CrossOrigin(origins = "*")
public class OptimizationController {

    @Autowired private ComparisonService comparisonService;
    @Autowired private AnalyticsService analyticsService;

    @GetMapping("/compare/{courierId}")
    public List<OptimizationReport> compare(
            @PathVariable Long courierId,
            @RequestParam String branchCity
    ) {
        return comparisonService.compareAll(courierId, branchCity);
    }

    // Dashboard için gerçek verileri çeken endpoint
    @GetMapping("/analytics")
    public Map<String, Object> getAnalytics() {
        return analyticsService.getDashboardAnalytics();
    }
}