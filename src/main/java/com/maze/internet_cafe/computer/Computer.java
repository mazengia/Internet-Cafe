package com.maze.internet_cafe.computer;

import com.maze.internet_cafe.branch.Branch;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "computers")
@Getter
@Setter
public class Computer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private OSType osType; // WINDOWS, LINUX

    private String macAddress;
    private String ipAddress;

    @Enumerated(EnumType.STRING)
    private ComputerStatus status;

    @ManyToOne
    private Branch branch;

    private LocalDateTime lastHeartbeat;
}
