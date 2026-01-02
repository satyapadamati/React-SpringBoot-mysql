package com.example.auth.controller;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
	    // Physical path must end with a slash /
	    registry.addResourceHandler("/uploads/**")
	            .addResourceLocations("file:///E:/java_projects/freelance_project's/spring-auth-app/uploads/");
	}
}
