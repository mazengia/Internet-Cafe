package com.maze.internet_cafe.computer;

import com.maze.internet_cafe.branch.Branch;
import com.maze.internet_cafe.utils.Auditable;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "computers")
@Getter
@Setter
public class Computer  extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    private OSType osType;
    private String macAddress;
    private String ipAddress;

    @Enumerated(EnumType.STRING)
    private ComputerStatus status = ComputerStatus.AVAILABLE;

    @ManyToOne
    private Branch branch;

    private LocalDateTime lastHeartbeat;
}
