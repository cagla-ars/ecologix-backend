package com.cagla.ecologix.service;

import com.cagla.ecologix.model.Branch;
import com.cagla.ecologix.repository.BranchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BranchService {

    @Autowired
    private BranchRepository branchRepository;

    public List<Branch> getAll() {
        return branchRepository.findAll();
    }

    public List<Branch> getByCity(String city) {
        return branchRepository.findByBranchCityIgnoreCase(city);
    }

    public Branch save(Branch branch) {
        return branchRepository.save(branch);
    }

    public Branch update(Long id, Branch updatedBranch) {

        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Şube bulunamadı"));

        branch.setBranchName(updatedBranch.getBranchName());
        branch.setBranchCity(updatedBranch.getBranchCity());
        branch.setBranchAddress(updatedBranch.getBranchAddress());
        branch.setBranchLat(updatedBranch.getBranchLat());
        branch.setBranchLon(updatedBranch.getBranchLon());

        return branchRepository.save(branch);
    }

    public void delete(Long id) {
        branchRepository.deleteById(id);
    }
}