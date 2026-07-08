package com.smart.parking.repository;

import com.smart.parking.domain.Reservation;
import com.smart.parking.domain.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Optional<Reservation> findByReference(String reference);
    List<Reservation> findByCustomerUser_IdOrderByCreatedAtDesc(Long userId);
    List<Reservation> findBySpace_Zone_Place_IdOrderByCreatedAtDesc(Long placeId);
    List<Reservation> findByStatus(ReservationStatus status);

    @Query("""
        SELECT COUNT(r) FROM Reservation r
        WHERE r.space.id = :spaceId
          AND r.status IN ('CONFIRMED','CHECKED_IN')
          AND r.startsAt < :endsAt AND r.endsAt > :startsAt
        """)
    long countOverlapping(@Param("spaceId") Long spaceId,
                          @Param("startsAt") LocalDateTime startsAt,
                          @Param("endsAt") LocalDateTime endsAt);

    @Query("SELECT r FROM Reservation r WHERE r.space.zone.place.id = :placeId AND r.startsAt >= :from AND r.startsAt < :to")
    List<Reservation> findByPlaceAndDateRange(@Param("placeId") Long placeId,
                                              @Param("from") LocalDateTime from,
                                              @Param("to") LocalDateTime to);

    @Query("SELECT r FROM Reservation r WHERE r.vehicle.plateNumber = :plate AND r.status IN ('CONFIRMED') AND r.startsAt <= :now AND r.endsAt >= :now")
    Optional<Reservation> findActiveReservationByPlate(@Param("plate") String plate, @Param("now") LocalDateTime now);
}
