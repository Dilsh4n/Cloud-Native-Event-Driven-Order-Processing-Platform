package com.orderplatform.inventory_service.repo;

import com.orderplatform.inventory_service.entity.ReservationStatus;
import com.orderplatform.inventory_service.entity.StockReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StockReservationRepository extends JpaRepository<StockReservation, UUID> {
    List<StockReservation> findByOrderIdAndStatus(UUID orderId, ReservationStatus status);
}
