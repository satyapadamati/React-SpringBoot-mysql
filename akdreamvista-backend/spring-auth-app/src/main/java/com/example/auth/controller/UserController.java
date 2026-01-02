package com.example.auth.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.auth.entity.User;
import com.example.auth.repository.UserRepository;
import com.example.auth.security.JwtUtil;

@RestController
@RequestMapping("/api/user")
public class UserController {

	private static final Logger logger = LoggerFactory.getLogger(UserController.class);

	private final JwtUtil jwtUtil;
	private final UserRepository userRepository;

	public UserController(JwtUtil jwtUtil, UserRepository userRepository) {
		this.jwtUtil = jwtUtil;
		this.userRepository = userRepository;
	}

	@GetMapping("/profile")
	public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String authHeader) {
		try {
			logger.info("Fetching user profile");

			if (authHeader == null || !authHeader.startsWith("Bearer ")) {
				logger.warn("Invalid Authorization header");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Authorization header");
			}

			String token = authHeader.replace("Bearer ", "");
			String email = jwtUtil.extractEmail(token);

			User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

			logger.info("User profile fetched successfully for email: {}", email);
			return ResponseEntity.ok(user);

		} catch (Exception e) {
			logger.error("Error while fetching user profile", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to fetch user profile");
		}
	}

	@GetMapping("/count")
	public ResponseEntity<?> getUserCount() {
		try {
			logger.info("Fetching total user count");

			int totalUsers = (int) userRepository.count();
			Map<String, Integer> counts = new HashMap<>();
			counts.put("total", totalUsers);

			logger.info("Total users count: {}", totalUsers);
			return ResponseEntity.ok(counts);

		} catch (Exception e) {
			logger.error("Error while fetching user count", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to fetch user count");
		}
	}
	
	@GetMapping("/all")
	public ResponseEntity<?> getAllUsers() {
	    try {
	        logger.info("Fetching all users");
	        return ResponseEntity.ok(userRepository.findAll());
	    } catch (Exception e) {
	        logger.error("Error fetching users", e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body("Failed to fetch users");
	    }
	}

}
