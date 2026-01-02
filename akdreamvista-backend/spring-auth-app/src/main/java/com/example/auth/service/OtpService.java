package com.example.auth.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Random;

@Service
public class OtpService {

	private final HashMap<String, String> otpStore = new HashMap<>();

	public String generateOtp(String email) {
		String otp = String.format("%06d", new Random().nextInt(999999));
		System.out.println("otp : " +  otp);
		otpStore.put(email, otp);
		return otp;
	}

	public boolean verifyOtp(String email, String otp) {
		return otpStore.containsKey(email) && otpStore.get(email).equals(otp);
	}
}
