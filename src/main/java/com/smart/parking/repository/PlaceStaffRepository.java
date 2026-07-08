package com.smart.parking.repository;

import com.smart.parking.domain.PlaceStaff;
import com.smart.parking.domain.enums.StaffRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface PlaceStaffRepository extends JpaRepository<PlaceStaff, Long> {
    List<PlaceStaff> findByPlace_IdAndActiveTrue(Long placeId);
    List<PlaceStaff> findByPlace_IdAndStaffRoleAndActiveTrue(Long placeId, StaffRole role);
    Optional<PlaceStaff> findByUser_IdAndActiveTrue(Long userId);
    boolean existsByUser_IdAndActiveTrue(Long userId);

    @Query("SELECT ps FROM PlaceStaff ps WHERE ps.user.id = :userId AND ps.active = true")
    Optional<PlaceStaff> findActiveByUserId(@Param("userId") Long userId);
}
