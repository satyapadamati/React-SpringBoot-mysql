package com.example.auth.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.auth.dto.LoginRequest;
import com.example.auth.dto.RegisterRequest;
import com.example.auth.entity.User;
import com.example.auth.model.Role;
import com.example.auth.repository.UserRepository;
import com.example.auth.security.JwtUtil;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public Map<String, Object> register(RegisterRequest request) {

        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(
                "ADMIN".equalsIgnoreCase(request.getRole())
                        ? Role.ADMIN
                        : Role.USER
        );

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        user.setToken(token);

        userRepository.save(user);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Registration successful");
        response.put("token", token);
        response.put("role", user.getRole().name());
        response.put("expiresIn", 7200);

        return response;
    }

    public Map<String, Object> loginWithRole(LoginRequest request) {
        logger.info("loginWithRole called");

        try {
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                throw new IllegalArgumentException("Email is required");
            }

            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                throw new IllegalArgumentException("Password is required");
            }

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Invalid credentials"));

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new RuntimeException("Invalid email or password");
            }

            String role = user.getRole().name();
            String token = jwtUtil.generateToken(user.getEmail(), role);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("id", user.getId());
            response.put("email", user.getEmail());
            response.put("role", role);
            response.put("expiresIn", 7200);

            return response;

        } catch (DataAccessException dae) {
            logger.error("Database error during login", dae);
            throw new RuntimeException("Database error during login");

        } catch (IllegalArgumentException iae) {
            logger.warn("Invalid input during login: {}", iae.getMessage());
            throw iae;

        } catch (RuntimeException re) {
            logger.warn("Login failed: {}", re.getMessage());
            throw re;

        } catch (Exception e) {
            logger.error("Unexpected error during login", e);
            throw new RuntimeException("Unexpected error occurred during login");
        }
    }
}
