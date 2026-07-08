package com.smart.parking.repository;

import com.smart.parking.domain.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    Optional<Vehicle> findByPlateNumber(String plateNumber);
    boolean existsByPlateNumber(String plateNumber);
    List<Vehicle> findByCustomerUser_Id(Long userId);
}
