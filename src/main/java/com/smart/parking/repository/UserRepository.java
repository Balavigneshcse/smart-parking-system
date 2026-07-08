package com.smart.parking.repository;

import com.smart.parking.domain.User;
import com.smart.parking.domain.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByRole(UserRole role);
    List<User> findByManagedState_Id(Long stateId);
    @Query("SELECT u FROM User u WHERE u.managedPlace.id = :placeId AND u.role = 'ADMIN'")
    Optional<User> findAdminByPlaceId(@Param("placeId") Long placeId);
}
