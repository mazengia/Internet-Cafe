package com.maze.internet_cafe.model;

import lombok.Data;

import jakarta.persistence.*;
import java.util.Set;

@Entity
@Data
public class Authority {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String name;
    @ManyToMany(mappedBy = "authorities")
    private Set<User> user;
}
