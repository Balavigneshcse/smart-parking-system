package com.smart.parking.repository;

import com.smart.parking.domain.PasswordResetOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface PasswordResetOtpRepository extends JpaRepository<PasswordResetOtp, Long> {
    Optional<PasswordResetOtp> findTopByEmailAndUsedFalseOrderByCreatedAtDesc(String email);

    @Modifying
    @Query("UPDATE PasswordResetOtp p SET p.used = true WHERE p.email = :email")
    void invalidateAllForEmail(@Param("email") String email);
}
