package com.cagla.ecologix.repository;

import com.cagla.ecologix.model.Branch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BranchRepository extends JpaRepository<Branch, Long> {

    List<Branch> findByBranchCityIgnoreCase(String branchCity);
}