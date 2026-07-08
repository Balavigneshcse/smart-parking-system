package com.smart.parking.repository;

import com.smart.parking.domain.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    List<ActivityLog>  findByUserIdOrderByTimestampDesc(Long userId);
    List<ActivityLog>  findByActionOrderByTimestampDesc(String action);
    Page<ActivityLog>  findAllByOrderByTimestampDesc(Pageable pageable);
    List<ActivityLog>  findBySuccessFalseOrderByTimestampDesc();

    @Query("SELECT a FROM ActivityLog a WHERE a.timestamp >= :from ORDER BY a.timestamp DESC")
    List<ActivityLog>  findRecent(@Param("from") LocalDateTime from);

    @Query("SELECT a FROM ActivityLog a WHERE a.timestamp >= :from AND a.timestamp < :to ORDER BY a.timestamp DESC")
    List<ActivityLog>  findByDateRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
