package com.smart.parking.repository;

import com.smart.parking.domain.State;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StateRepository extends JpaRepository<State, Long> {
    Optional<State> findByCode(String code);
    boolean existsByCode(String code);
}
