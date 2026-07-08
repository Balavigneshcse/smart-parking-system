package com.smart.parking.repository;

import com.smart.parking.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByReservation_Id(Long reservationId);
    Optional<Payment> findBySession_Id(Long sessionId);
    List<Payment> findBySession_Place_Id(Long placeId);
}
