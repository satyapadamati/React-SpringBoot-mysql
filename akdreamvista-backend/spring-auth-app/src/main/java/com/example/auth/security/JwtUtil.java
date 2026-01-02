package com.example.auth.security;

import java.security.Key;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

	private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

	@Value("${jwt.secret}")
	private String secret;

	@Value("${jwt.expirationMs}")
	private long jwtExpirationMs;

	private Key getSigningKey() {
		return Keys.hmacShaKeyFor(secret.getBytes());
	}

	public String generateToken(String email, String role) {
		System.out.println("[generateToken] Email: " + email);
		System.out.println("[generateToken] Role: " + role);

		String token = Jwts.builder().setSubject(email).claim("role", role).setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
				.signWith(getSigningKey(), SignatureAlgorithm.HS256).compact();

		System.out.println("[generateToken] Token generated: " + token);

		return token;
	}

	public boolean validateToken(String token) {
		try {
			Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);

			// token parsed successfully → now check expiry
			return !isTokenExpired(token);

		} catch (Exception e) {
			logger.error("Invalid JWT Token: {}", e.getMessage());
			return false;
		}
	}

	public String extractEmail(String token) {
		System.out.println("[extractEmail] Token received: " + token);

		String email = Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody()
				.getSubject();

		System.out.println("[extractEmail] Extracted email: " + email);
		return email;
	}

	public boolean isTokenExpired(String token) {
		System.out.println("[isTokenExpired] Checking token: " + token);
		Date expiration = Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody()
				.getExpiration();
		System.out.println("[isTokenExpired] Expiration date: " + expiration);
		boolean expired = expiration.before(new Date());
		System.out.println("[isTokenExpired] Is expired? " + expired);

		return expired;
	}

	// 🔥 REQUIRED METHOD (THIS FIXES YOUR ERROR)
	public String extractRole(String token) {
		return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody().get("role",
				String.class);
	}
	public String extractUsername(String token) {
		return extractEmail(token);
	}


}
