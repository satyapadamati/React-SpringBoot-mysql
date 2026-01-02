package com.example.auth.controller;
import java.security.SecureRandom;
import java.util.Base64;

public class GenerateJwtKey {
	 public static void main(String[] args) {
	        byte[] key = new byte[32]; // 256 bit
	        new SecureRandom().nextBytes(key);
	        System.out.println(Base64.getEncoder().encodeToString(key));
	    }//+eAQ8NGLzhjXW/nlown/DUCy6kKzQU8UkJtaGv7U5ZU=

}
