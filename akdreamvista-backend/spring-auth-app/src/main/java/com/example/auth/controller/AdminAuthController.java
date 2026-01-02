package com.example.auth.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.auth.entity.User;
import com.example.auth.model.Role;
import com.example.auth.repository.UserRepository;
import com.example.auth.security.JwtUtil;

@RestController
@RequestMapping("/api/admin")
public class AdminAuthController {

	private static final Logger logger = LoggerFactory.getLogger(AdminAuthController.class);

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtil jwtUtil;

	public AdminAuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtUtil = jwtUtil;
	}

	// ---------------- ADMIN SIGNUP -------------------
	@PostMapping("/signup")
	public ResponseEntity<?> adminSignup(@RequestBody User user) {

		// System.out.println("Received Admin Signup Request : "+ user.getEmail());

		if (!user.getEmail().contains("@")) {
			// System.out.println("Invalid email format: {}"+ user.getEmail());
			return ResponseEntity.badRequest().body("Invalid Email");
		}

		if (userRepository.existsByEmail(user.getEmail())) {
			// System.out.println("Email already exists: {}"+ user.getEmail());
			return ResponseEntity.badRequest().body("Email already exists");
		}

		user.setRole(Role.ADMIN);
		user.setPassword(passwordEncoder.encode(user.getPassword()));

		// Generate Token
		String token = jwtUtil.generateToken(user.getEmail(), "ADMIN");
		user.setToken(token);
		// System.out.println(" token : "+ token +" saving...." + user.getToken());
		// SAVE USER WITH TOKEN
		User savedUser = userRepository.save(user);
		logger.info("Admin Registered. Token saved for User ID {}: {}", savedUser.getId(), token);

		return ResponseEntity.ok(Map.of("message", "Admin registered successfully", "token", token, "expiresIn", 13600,
				"role", "ADMIN"));
	}

	// ---------------- ADMIN LOGIN -------------------
	@PostMapping("/login")
	public ResponseEntity<?> adminLogin(
	    @RequestBody User request, 
	    @RequestHeader(value = "X-Admin-Secret", required = false) String secret) {

	    String ACTUAL_SECRET = "AKDV@2025";

	    if (secret == null || !secret.equals(ACTUAL_SECRET)) {
	        logger.warn("Unauthorized access attempt to Admin Login API");
	        return ResponseEntity.status(403).body(Map.of("message", "Forbidden: Invalid Secret Code"));
	    }
		logger.info("Admin Login Attempt: {}", request.getEmail());

		User existing = userRepository.findByEmail(request.getEmail()).orElse(null);

		if (existing == null) {
			logger.warn("Login failed, email not found: {}", request.getEmail());
			// Return JSON with message
			Map<String, String> error = new HashMap<>();
			error.put("message", "Invalid email or password");
			return ResponseEntity.status(401).body(error);
		}

		if (!passwordEncoder.matches(request.getPassword(), existing.getPassword())) {
			logger.warn("Login failed: wrong password for {}", request.getEmail());
			// Return JSON with message
			Map<String, String> error = new HashMap<>();
			error.put("message", "Wrong password");
			return ResponseEntity.status(401).body(error);
		}

		String token = jwtUtil.generateToken(existing.getEmail(), existing.getRole().name());
		logger.info("Login successful, token generated: {}", token);

		existing.setToken(token);
		userRepository.save(existing);

		// Response HashMap
		Map<String, Object> response = new HashMap<>();
		response.put("token", token);
		response.put("role", existing.getRole().name());
		response.put("expiresIn", 13600);

		return ResponseEntity.ok(response);
	}

	// ---------------- FORGOT PASSWORD -------------------
	@PostMapping("/forgot-password")
	public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> req) {

		String email = req.get("email");
		String newPassword = req.get("password");

		logger.info("Admin forgot password request for: {}", email);

		if (email == null || newPassword == null || newPassword.length() < 6) {
			return ResponseEntity.badRequest().body("Invalid email or password");
		}

		User user = userRepository.findByEmail(email).orElse(null);

		if (user == null) {
			logger.warn("Email not found: {}", email);
			return ResponseEntity.badRequest().body("Email not found");
		}

		// Update password directly
		user.setPassword(passwordEncoder.encode(newPassword));
		userRepository.save(user);

		logger.info("Password updated successfully for {}", email);

		return ResponseEntity.ok("Password updated successfully");
	}

	// ---------------- RESET PASSWORD -------------------
	@PostMapping("/reset-password")
	public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> req) {

		String email = req.get("email");
		String otp = req.get("otp");
		String newPassword = req.get("password");

		logger.info("Password reset attempt for: {}", email);

		User user = userRepository.findByEmail(email).orElse(null);

		if (user == null || !otp.equals(user.getResetOtp())) {
			logger.warn("Invalid OTP for {}", email);
			return ResponseEntity.badRequest().body("Invalid OTP");
		}

		user.setPassword(passwordEncoder.encode(newPassword));
		user.setResetOtp(null);
		userRepository.save(user);

		logger.info("Password reset successful for {}", email);

		return ResponseEntity.ok("Password reset successful");
	}

}
