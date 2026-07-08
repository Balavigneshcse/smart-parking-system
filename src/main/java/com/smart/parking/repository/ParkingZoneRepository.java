package com.smart.parking.repository;

import com.smart.parking.domain.ParkingZone;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ParkingZoneRepository extends JpaRepository<ParkingZone, Long> {
    List<ParkingZone> findByPlace_Id(Long placeId);
    List<ParkingZone> findByPlace_IdOrderByNameAsc(Long placeId);
}
