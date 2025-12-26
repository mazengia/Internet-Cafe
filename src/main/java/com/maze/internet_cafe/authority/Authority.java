package com.maze.internet_cafe.authority;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.maze.internet_cafe.users.User;
import com.maze.internet_cafe.utils.Auditable;
import lombok.Data;

import jakarta.persistence.*;
import java.util.Set;

@Entity
@Data
public class Authority  extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String name;
    @ManyToMany(mappedBy = "authorities")
    @JsonIgnore
    private Set<User> user;
}
