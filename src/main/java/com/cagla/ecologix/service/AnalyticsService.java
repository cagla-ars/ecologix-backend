package com.cagla.ecologix.service;

import com.cagla.ecologix.repository.OptimizationHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class AnalyticsService {

    @Autowired
    private OptimizationHistoryRepository historyRepository;

    public Map<String, Object> getDashboardAnalytics() {
        Double totalSaved = historyRepository.getTotalCarbonSaved();
        Double totalDist = historyRepository.getTotalDistanceCovered();
        long totalCount = historyRepository.count();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCarbonSaved", totalSaved != null ? totalSaved : 0.0);
        stats.put("totalDistance", totalDist != null ? totalDist : 0.0);
        stats.put("totalOptimizations", totalCount);
        
        double trees = totalSaved != null ? (totalSaved / 22.0) : 0.0;
        stats.put("treesSaved", Math.round(trees * 100.0) / 100.0);
        
        return stats;
    }
}