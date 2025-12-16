package com.maze.internet_cafe.computer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ComputerRepository extends JpaRepository<Computer, Long> {
    Optional<Computer> findByMacAddress(String macAddress);
    Page<Computer> findByBranchId(Long branchId, Pageable pageable);
}
