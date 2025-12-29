package com.maze.internet_cafe.computer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ComputerRepository extends JpaRepository<Computer, Long> {
    Optional<Computer> findByName(String name);
    Optional<Computer> findByMacAddress(String macAddress);
    Page<Computer> findByBranchId(Long branchId, Pageable pageable);

    List<Computer> findByLastHeartbeatBeforeAndStatusNot(Instant cutoff, ComputerStatus computerStatus);
}
