package com.smart.parking.repository;

import com.smart.parking.domain.ParkingPlace;
import com.smart.parking.domain.enums.PlaceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ParkingPlaceRepository extends JpaRepository<ParkingPlace, Long> {
    List<ParkingPlace> findByActiveTrue();
    List<ParkingPlace> findByState_IdAndActiveTrue(Long stateId);
    List<ParkingPlace> findByCategoryAndActiveTrue(PlaceCategory category);
    @Query("SELECT p FROM ParkingPlace p WHERE p.active = true ORDER BY p.name")
    List<ParkingPlace> findAllActiveSorted();
    @Query("SELECT COUNT(s) FROM ParkingSpace s WHERE s.zone.place.id = :placeId AND s.status = 'AVAILABLE'")
    long countAvailableSpaces(@Param("placeId") Long placeId);
    @Query("SELECT COUNT(s) FROM ParkingSpace s WHERE s.zone.place.id = :placeId")
    long countTotalSpaces(@Param("placeId") Long placeId);
}
