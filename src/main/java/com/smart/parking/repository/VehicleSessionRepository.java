package com.smart.parking.repository;

import com.smart.parking.domain.VehicleSession;
import com.smart.parking.domain.enums.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VehicleSessionRepository extends JpaRepository<VehicleSession, Long> {
    List<VehicleSession> findByPlace_IdAndStatus(Long placeId, SessionStatus status);
    Optional<VehicleSession> findByVehicle_PlateNumberAndStatus(String plate, SessionStatus status);

    @Query("SELECT vs FROM VehicleSession vs WHERE vs.place.id = :placeId AND vs.entryTime >= :from AND vs.entryTime < :to")
    List<VehicleSession> findByPlaceAndDateRange(@Param("placeId") Long placeId,
                                                 @Param("from") LocalDateTime from,
                                                 @Param("to") LocalDateTime to);

    @Query("SELECT COALESCE(SUM(vs.totalFee), 0) FROM VehicleSession vs WHERE vs.place.id = :placeId AND vs.entryTime >= :from AND vs.entryTime < :to AND vs.status = 'COMPLETED'")
    BigDecimal sumRevenueByPlaceAndDateRange(@Param("placeId") Long placeId,
                                             @Param("from") LocalDateTime from,
                                             @Param("to") LocalDateTime to);

    @Query("SELECT COUNT(vs) FROM VehicleSession vs WHERE vs.place.id = :placeId AND vs.entryTime >= :from AND vs.entryTime < :to")
    long countByPlaceAndDateRange(@Param("placeId") Long placeId,
                                  @Param("from") LocalDateTime from,
                                  @Param("to") LocalDateTime to);
}
