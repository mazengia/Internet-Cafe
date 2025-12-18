package com.maze.internet_cafe.service;

import com.maze.internet_cafe.model.Authority;
import com.maze.internet_cafe.model.AuthorityRepository;
import com.maze.internet_cafe.model.User;
import com.maze.internet_cafe.model.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
public class DataLoader {

    @Bean
    public CommandLineRunner createDefaultUsers(UserRepository userRepository, AuthorityRepository authorityRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByUsername("admin").isEmpty()) {
                Authority a1 = authorityRepository.findByName("ROLE_ADMIN").orElseGet(() -> {
                    Authority na = new Authority();
                    na.setName("ROLE_ADMIN");
                    return authorityRepository.save(na);
                });
                Authority a2 = authorityRepository.findByName("ROLE_USER").orElseGet(() -> {
                    Authority na = new Authority();
                    na.setName("ROLE_USER");
                    return authorityRepository.save(na);
                });

                User u = new User();
                u.setName("Administrator");
                u.setUsername("admin");
                u.setPassword(passwordEncoder.encode("admin"));
                u.setEnabled(true);
                u.setAccountNonExpired(true);
                u.setAccountNonLocked(true);
                u.setCredentialsNonExpired(true);

                u.setAuthorities(List.of(a1, a2));
                userRepository.save(u);
            }
        };
    }
}
