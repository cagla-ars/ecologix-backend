package com.cagla.ecologix.controller;

import com.cagla.ecologix.model.Branch;
import com.cagla.ecologix.service.BranchService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/branches")
@CrossOrigin(origins = "*")
public class BranchController {

    @Autowired
    private BranchService branchService;

    @GetMapping
    public List<Branch> getAllBranches() {
        return branchService.getAll();
    }

    @GetMapping("/city/{city}")
    public List<Branch> getBranchesByCity(@PathVariable String city) {
        return branchService.getByCity(city);
    }

    @PostMapping
    public Branch createBranch(@RequestBody Branch branch) {
        return branchService.save(branch);
    }

    @PutMapping("/{id}")
    public Branch updateBranch(
            @PathVariable Long id,
            @RequestBody Branch branch) {

        return branchService.update(id, branch);
    }

    @DeleteMapping("/{id}")
    public void deleteBranch(@PathVariable Long id) {
        branchService.delete(id);
    }
}
