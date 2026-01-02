package com.example.auth.service;

import org.springframework.stereotype.Service;

@Service
public class JwtService {
//
//	@Value("${jwt.secret}")
//	private String secret;
//
//	@Value("${jwt.expiration}")
//	private long jwtExpirationMs;
//
//	private Key getSignKey() {
//		return Keys.hmacShaKeyFor(io.jsonwebtoken.io.Decoders.BASE64.decode(secret));
//	}
//
//	public String generateToken(UserDetails userDetails, String role) {
//		Map<String, Object> claims = new HashMap<>();
//		claims.put("role", role);
//		return createToken(claims, userDetails.getUsername());
//	}
//
//	private String createToken(Map<String, Object> claims, String subject) {
//		Date now = new Date();
//		Date expiry = new Date(now.getTime() + jwtExpirationMs); // 1 hour
//
//		return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(now).setExpiration(expiry)
//				.signWith(getSignKey(), SignatureAlgorithm.HS256).compact();
//	}
//
//	public String extractUsername(String token) {
//		return extractClaim(token, Claims::getSubject);
//	}
//
//	public String extractRole(String token) {
//		return extractAllClaims(token).get("role", String.class);
//	}
//
//	public <T> T extractClaim(String token, Function<Claims, T> resolver) {
//		final Claims claims = extractAllClaims(token);
//		return resolver.apply(claims);
//	}
//
//	private Claims extractAllClaims(String token) {
//		return Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token).getBody();
//	}
//
//	public boolean isTokenValid(String token, UserDetails userDetails) {
//		final String username = extractUsername(token);
//		return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
//	}
//
//	private boolean isTokenExpired(String token) {
//		return extractExpiration(token).before(new Date());
//	}
//
//	private Date extractExpiration(String token) {
//		return extractClaim(token, Claims::getExpiration);
//	}
}
