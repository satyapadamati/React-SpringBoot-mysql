package com.example.auth.repository;

public interface SmsService {
	void sendPaymentConfirmation(String mobile, String message);
}
