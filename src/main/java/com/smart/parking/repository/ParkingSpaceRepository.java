package com.smart.parking.repository;

import com.smart.parking.domain.ParkingSpace;
import com.smart.parking.domain.enums.SpaceStatus;
import com.smart.parking.domain.enums.SpaceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ParkingSpaceRepository extends JpaRepository<ParkingSpace, Long> {
    List<ParkingSpace> findByZone_Id(Long zoneId);
    List<ParkingSpace> findByZone_Place_IdAndSpaceType(Long placeId, SpaceType type);
    List<ParkingSpace> findByZone_Place_Id(Long placeId);
    List<ParkingSpace> findByZone_Place_IdAndStatus(Long placeId, SpaceStatus status);

    @Query("""
        SELECT s FROM ParkingSpace s
        WHERE s.zone.place.id = :placeId
          AND s.spaceType = :type
          AND s.status = 'AVAILABLE'
          AND s.id NOT IN (
            SELECT r.space.id FROM Reservation r
            WHERE r.status IN ('CONFIRMED','CHECKED_IN')
              AND r.startsAt < :endsAt AND r.endsAt > :startsAt
          )
        ORDER BY s.code ASC
        """)
    List<ParkingSpace> findAvailableForReservation(
            @Param("placeId") Long placeId,
            @Param("type") SpaceType type,
            @Param("startsAt") LocalDateTime startsAt,
            @Param("endsAt") LocalDateTime endsAt);

    @Query("SELECT s FROM ParkingSpace s WHERE s.zone.place.id = :placeId AND s.status = 'AVAILABLE' AND s.spaceType = :type ORDER BY s.code ASC")
    Optional<ParkingSpace> findFirstAvailableSpace(@Param("placeId") Long placeId, @Param("type") SpaceType type);
}
