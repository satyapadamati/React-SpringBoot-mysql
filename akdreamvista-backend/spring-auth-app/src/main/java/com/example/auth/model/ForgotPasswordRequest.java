package com.example.auth.model;

public record ForgotPasswordRequest(String email, String otp, String newPassword) {
}
