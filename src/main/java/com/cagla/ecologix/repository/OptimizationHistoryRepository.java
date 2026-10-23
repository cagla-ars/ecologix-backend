package com.cagla.ecologix.repository;

import com.cagla.ecologix.model.OptimizationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OptimizationHistoryRepository extends JpaRepository<OptimizationHistory, Long> {
    
    @Query("SELECT SUM(o.carbonSaved) FROM OptimizationHistory o")
    Double getTotalCarbonSaved();

    @Query("SELECT SUM(o.totalDistance) FROM OptimizationHistory o")
    Double getTotalDistanceCovered();
}