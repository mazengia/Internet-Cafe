package com.maze.internet_cafe.session;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Long> {
    List<Session> findByStatus(SessionStatus status);
    Optional<Session> findByComputerIdAndStatus(Long computerId, SessionStatus status);
    Page<Session> findByComputerId(Long computerId, Pageable pageable);

     @Query("SELECT function('date', s.endTime), COUNT(s), SUM(s.totalMinutes), SUM(s.totalCost) " +
            "FROM Session s WHERE s.endTime BETWEEN :from AND :to GROUP BY function('date', s.endTime) ORDER BY function('date', s.endTime)")
    List<Object[]> dailyAggregationByDate(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    List<Session> findAllByComputerIdAndStatus(Long id, SessionStatus sessionStatus);
}

