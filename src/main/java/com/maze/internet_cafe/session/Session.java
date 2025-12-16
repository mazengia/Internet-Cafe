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
    private Branch branch;

    @ManyToOne
    private Computer computer;

    @ManyToOne
    private User user;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private BigDecimal pricePerHour;
    private Long totalMinutes;
    private BigDecimal totalCost;

    @Enumerated(EnumType.STRING)
    private SessionStatus status;

}
