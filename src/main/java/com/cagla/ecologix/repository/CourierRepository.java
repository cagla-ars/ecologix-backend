package com.cagla.ecologix.repository;

import com.cagla.ecologix.model.Courier;
import com.cagla.ecologix.model.enums.CourierStatus;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourierRepository extends JpaRepository<Courier, Long> {
    List<Courier> findByStatus(CourierStatus status);
}