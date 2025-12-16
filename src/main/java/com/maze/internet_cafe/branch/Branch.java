package com.maze.internet_cafe.branch;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "branches")
@Getter
@Setter
public class Branch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String city;

    @Column(nullable = false)
    private BigDecimal pricePerHour;

    private LocalDateTime createdAt = LocalDateTime.now();
}
