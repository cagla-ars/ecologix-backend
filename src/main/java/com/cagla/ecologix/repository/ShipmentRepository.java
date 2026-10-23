package com.cagla.ecologix.repository;

import com.cagla.ecologix.model.Shipment;
import com.cagla.ecologix.model.enums.ShipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    List<Shipment> findByStatus(ShipmentStatus status);

    List<Shipment> findByCurrentHubAndStatus(String currentHub, ShipmentStatus status);

    List<Shipment> findByCurrentHubCityAndStatus(String currentHubCity, ShipmentStatus status);

    List<Shipment> findByCurrentHubCityAndDestinationCityAndStatus(
            String currentHubCity,
            String destinationCity,
            ShipmentStatus status
    );

    List<Shipment> findByCourierIdAndStatusOrderByDeliveryIndexAsc(
            Long courierId,
            ShipmentStatus status
    );
    
    List<Shipment> findByStatusAndDeliveryIndexIsNotNullOrderByDeliveryIndexAsc(
            ShipmentStatus status
    );
    long countByStatus(ShipmentStatus status);

    long countByCourierIdAndStatusAndDeliveredAtBetween(
            Long courierId,
            ShipmentStatus status,
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("SELECT COUNT(s) FROM Shipment s WHERE s.status = com.cagla.ecologix.model.enums.ShipmentStatus.DELIVERED")
    long countDeliveredShipments();

    Optional<Shipment> findByTrackingNumber(String trackingNumber);
}
