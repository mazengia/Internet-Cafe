package com.maze.internet_cafe.session;

import com.maze.internet_cafe.branch.Branch;
import com.maze.internet_cafe.computer.Computer;
import com.maze.internet_cafe.model.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sessions")
@Getter
@Setter
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Computer computer;


    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private Long totalMinutes;

    @Enumerated(EnumType.STRING)
    private SessionStatus status;

    @Column(precision = 10, scale = 2)
    private BigDecimal pricePerHour;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalCost;


}
