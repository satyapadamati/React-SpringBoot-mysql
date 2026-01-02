package com.example.auth.config;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.auth.security.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

	@Autowired
	private JwtUtil jwtUtil;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		try {

			String path = request.getRequestURI();
			if (path.startsWith("/uploads/")) {
				filterChain.doFilter(request, response);
				return;
			}

			// ✅ ONLY PUBLIC AUTH APIs
			if (path.startsWith("/api/auth") || path.startsWith("/api/admin/login")
					|| path.startsWith("/api/admin/signup") || path.startsWith("/api/admin/forgot-password")
					|| path.startsWith("/api/admin/reset-password") || path.equals("/api/properties/all")
					|| path.startsWith("/api/properties/")|| path.startsWith("/api/payment/")) {

				filterChain.doFilter(request, response);
				return;
			}

			// 🔐 JWT REQUIRED FOR ALL OTHER APIs
			String authHeader = request.getHeader("Authorization");

			if (authHeader == null || !authHeader.startsWith("Bearer ")) {
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				return;
			}

			String token = authHeader.substring(7);

			if (!jwtUtil.validateToken(token)) {
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				return;
			}

			String email = jwtUtil.extractEmail(token);
			String role = jwtUtil.extractRole(token); // USER / ADMIN

			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(email, null,
					List.of(new SimpleGrantedAuthority("ROLE_" + role)));

			SecurityContextHolder.getContext().setAuthentication(authentication);

			filterChain.doFilter(request, response);
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		}
	}
}
