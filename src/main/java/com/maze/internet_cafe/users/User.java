/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.maze.internet_cafe.users;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.maze.internet_cafe.authority.Authority;
import com.maze.internet_cafe.utils.Auditable;
import lombok.Data;

import jakarta.persistence.*;

import java.util.List;


@Entity(name = "users")
@Data
public class User  extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private String role;
    @Column(unique = true)
    private String username;
    @JsonIgnore
    private String password;
    private boolean enabled;
    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;
    @ManyToMany(fetch = FetchType.EAGER)
    private List<Authority> authorities;

}
