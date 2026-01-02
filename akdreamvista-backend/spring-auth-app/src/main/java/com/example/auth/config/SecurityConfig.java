package com.example.auth.config;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

	@Autowired
	private JwtAuthFilter jwtAuthFilter;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable()).cors(Customizer.withDefaults())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/uploads/**").permitAll()
						// ✅ PUBLIC
						.requestMatchers("/api/auth/**", "/api/admin/login", "/api/admin/signup",
								"/api/admin/forgot-password", "/api/admin/reset-password", "/api/properties/all", "/api/properties/{id}", "/api/payment/**")
						.permitAll()

						// 🔐 ADMIN ONLY
						.requestMatchers("/api/admin/**").hasRole("ADMIN")

						// 🔐 USER + ADMIN
						.requestMatchers("/api/properties/**", "/api/payment/**", "/api/user/**", "/api/properties/update/**")
						.hasAnyRole("USER", "ADMIN")

						.anyRequest().authenticated())

				.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {

    		CorsConfiguration config = new CorsConfiguration();

    		config.setAllowCredentials(true);

    		// ✅ Allow your frontend URLs
    		config.setAllowedOrigins(List.of(
            		"http://localhost:5173",
            		"http://23.20.0.192:5173"
    		));

    		config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    		config.setAllowedHeaders(List.of("*"));

   		 UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    		source.registerCorsConfiguration("/**", config);

    		return source;
	}
}
