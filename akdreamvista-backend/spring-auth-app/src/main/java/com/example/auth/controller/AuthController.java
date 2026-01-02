package com.example.auth.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.auth.dto.LoginRequest;
import com.example.auth.dto.RegisterRequest;
import com.example.auth.entity.User;
import com.example.auth.model.ForgotPasswordDirectRequest;
import com.example.auth.model.ForgotPasswordRequest;
import com.example.auth.model.OtpRequest;
import com.example.auth.model.Role;
import com.example.auth.repository.UserRepository;
import com.example.auth.service.AuthService;
import com.example.auth.service.EmailService;
import com.example.auth.service.OtpService;
import com.example.auth.service.UserService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

	private final AuthService authService;
	private final UserRepository userRepository;
	private final OtpService otpService;
	private final EmailService emailService;
	private final UserService userService;
	private final PasswordEncoder passwordEncoder;

	public AuthController(PasswordEncoder passwordEncoder, AuthService authService, UserRepository userRepository,
			OtpService otpService, EmailService emailService, UserService userService) {

		this.authService = authService;
		this.userRepository = userRepository;
		this.otpService = otpService;
		this.emailService = emailService;
		this.userService = userService;
		this.passwordEncoder = passwordEncoder;
	}

	// ---------------- SIGNUP ----------------
	@PostMapping("/signup")
	public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
		logger.info("POST /signup called with email: {}", request.getEmail());
		try {
			if (userRepository.existsByEmail(request.getEmail())) {
				logger.warn("Signup failed: Email already exists - {}", request.getEmail());
				return ResponseEntity.status(409).body("Email already exists");
			}

			Map<String, Object> response = authService.register(request);
			logger.info("Signup successful for email: {}", request.getEmail());
			return ResponseEntity.ok(response);

		} catch (Exception e) {
			logger.error("Unexpected error during signup for email {}: {}", request.getEmail(), e.getMessage(), e);
			return ResponseEntity.status(500).body("Signup failed due to server error");
		}
	}

	// ---------------- LOGIN ----------------
	@PostMapping("/signin")
	public ResponseEntity<?> login(@RequestBody LoginRequest request) {
		logger.info("POST /signin called with email: {}", request.getEmail());
		try {
			if (request.getEmail() == null || request.getEmail().isBlank()) {
				logger.warn("Login failed: Email is required");
				return ResponseEntity.badRequest().body("Email is required");
			}
			if (request.getPassword() == null || request.getPassword().isBlank()) {
				logger.warn("Login failed: Password is required for email {}", request.getEmail());
				return ResponseEntity.badRequest().body("Password is required");
			}

			Map<String, Object> result = authService.loginWithRole(request);
			logger.info("Login successful for email: {}", request.getEmail());
			return ResponseEntity.ok(result);

		} catch (RuntimeException e) {
		    logger.warn("Login failed for email {}: {}", request.getEmail(), e.getMessage());
		    return ResponseEntity.status(401).body(Map.of("message", "Invalid email or password"));
		}catch (Exception e) {
			logger.error("Unexpected error during login for email {}: {}", request.getEmail(), e.getMessage(), e);
			return ResponseEntity.status(500).body("Login failed due to server error");
		}
	}

	// ---------------- ADMIN LOGIN ----------------
	@PostMapping("/admin-login")
	public ResponseEntity<?> adminLogin(@RequestBody LoginRequest request) {
		logger.info("POST /admin-login called with email: {}", request.getEmail());
		try {
			Map<String, Object> result = authService.loginWithRole(request);

			if (!"ADMIN".equals(result.get("role"))) {
				logger.warn("Admin login denied for email: {}", request.getEmail());
				return ResponseEntity.status(403).body("Access denied");
			}

			logger.info("Admin login successful for email: {}", request.getEmail());
			return ResponseEntity.ok(result);

		} catch (RuntimeException e) {
			logger.warn("Admin login failed for email {}: {}", request.getEmail(), e.getMessage());
			return ResponseEntity.status(401).body("Invalid email or password");
		} catch (Exception e) {
			logger.error("Unexpected error during admin login for email {}: {}", request.getEmail(), e.getMessage(), e);
			return ResponseEntity.status(500).body("Admin login failed due to server error");
		}
	}

	// ---------------- GOOGLE LOGIN ----------------
	@PostMapping("/google-login")
	public ResponseEntity<?> googleLogin(@RequestBody RegisterRequest req) {
		logger.info("POST /google-login called with email: {}", req.getEmail());
		try {
			User user = userRepository.findByEmail(req.getEmail()).orElseGet(() -> {
				User u = new User();
				u.setEmail(req.getEmail());
				u.setPassword(req.getEmail()); // dummy password
				u.setRole(Role.USER);
				return userRepository.save(u);
			});

			logger.info("Google login successful for email: {}", req.getEmail());
			return ResponseEntity.ok(Map.of("token", "", "role", user.getRole().name()));

		} catch (Exception e) {
			logger.error("Unexpected error during Google login for email {}: {}", req.getEmail(), e.getMessage(), e);
			return ResponseEntity.status(500).body("Google login failed");
		}
	}

	// ---------------- SEND OTP ----------------
	@PostMapping("/send-otp")
	public ResponseEntity<?> sendOtp(@RequestBody OtpRequest request) {
		logger.info("POST /send-otp called for email: {}", request.email());
		try {
			String otp = otpService.generateOtp(request.email());
			logger.info("Generated OTP for email: {}", request.email()); // safer than logging OTP
			logger.info("Generated OTP : {}", otp);
			emailService.sendSimpleMessage(request.email(), "Your OTP Code", "Your OTP is: " + otp);

			logger.info("OTP sent successfully to email: {}", request.email());
			return ResponseEntity.ok("OTP sent");

		} catch (Exception e) {
			logger.error("Failed to send OTP for email {}: {}", request.email(), e.getMessage(), e);
			return ResponseEntity.status(500).body("Failed to send OTP");
		}
	}

	// ---------------- RESET PASSWORD ----------------
	@PostMapping("/reset-password")
	public ResponseEntity<?> resetPassword(@RequestBody ForgotPasswordRequest req) {
		logger.info("POST /reset-password called for email: {}", req.email());
		try {
			if (!otpService.verifyOtp(req.email(), req.otp())) {
				logger.warn("Password reset failed: Invalid OTP for email {}", req.email());
				return ResponseEntity.status(400).body("Invalid OTP");
			}

			userService.updatePassword(req.email(), req.newPassword());
			logger.info("Password reset successful for email: {}", req.email());
			return ResponseEntity.ok("Password updated successfully");

		} catch (Exception e) {
			logger.error("Unexpected error during password reset for email {}: {}", req.email(), e.getMessage(), e);
			return ResponseEntity.status(500).body("Password reset failed");
		}
	}

	@PostMapping("/forgot-password")
	public ResponseEntity<?> userForgotPassword(@RequestBody ForgotPasswordDirectRequest req) {

		String email = req.getEmail();
		String newPassword = req.getPassword();

		logger.info("User forgot password request for: {}", email);

		if (email == null || email.isBlank() || newPassword == null || newPassword.length() < 3) {
			return ResponseEntity.badRequest().body("Invalid email or password");
		}

		User user = userRepository.findByEmail(email).orElse(null);

		if (user == null) {
			return ResponseEntity.badRequest().body("Email not found");
		}

		user.setPassword(passwordEncoder.encode(newPassword));
		userRepository.save(user);

		return ResponseEntity.ok("Password updated successfully");
	}

}
