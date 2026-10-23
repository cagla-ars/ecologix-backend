package com.cagla.ecologix.controller;

import com.cagla.ecologix.model.Courier;
import com.cagla.ecologix.service.CourierService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/couriers") 
@CrossOrigin(origins = "*")
public class CourierController {
    
    @Autowired
    private CourierService courierService;

    @GetMapping
    public List<Courier> getAllCouriers() {
        return courierService.getAll(); 
    }

    @PostMapping
    public Courier createCourier(@RequestBody Courier courier) {
        return courierService.save(courier); 
    }

   @PutMapping("/{id}/vehicle")
    public Courier updateVehicle(@PathVariable Long id,
                                @RequestParam String type) {

        return courierService.updateVehicle(id, type);
    }
    
    @PatchMapping("/{id}/location")
    public Courier updateLocation(@PathVariable Long id, 
                                @RequestParam Double lat, 
                                @RequestParam Double lon) {
        return courierService.updateLocation(id, lat, lon);
    }

    @PatchMapping("/{id}/status")
    public Courier updateStatus(@PathVariable Long id, 
                                @RequestParam String status) {
        return courierService.updateStatus(id, status);
    }
}