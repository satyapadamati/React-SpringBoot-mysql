package com.example.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import com.example.auth.controller.AdminAuthController;

@SpringBootApplication
@ComponentScan(basePackages = "com.example.auth")
public class AuthApplication {
	private static final Logger logger = LoggerFactory.getLogger(AdminAuthController.class);
	
    public static void main(String[] args) {
    	logger.info("Spring Boot Application is running...");
        SpringApplication.run(AuthApplication.class, args);
    }
}


