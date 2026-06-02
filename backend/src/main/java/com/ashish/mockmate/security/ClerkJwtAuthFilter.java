package com.ashish.mockmate.security;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ClerkJwtAuthFilter extends OncePerRequestFilter {

	@Value("${clerk.jwks-url}")
	private String jwksUrl;

	@Value("${clerk.issuer}")
	private String expectedIssuer;

	private ConfigurableJWTProcessor<SecurityContext> jwtProcessor;

	@PostConstruct
	public void init() throws Exception {
		jwtProcessor = new DefaultJWTProcessor<>();
		JWKSource<SecurityContext> keySource = new RemoteJWKSet<>(new URL(jwksUrl));
		JWSKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, keySource);
		jwtProcessor.setJWSKeySelector(keySelector);
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String token = extractToken(request);
		if (token != null) {
			try {
				JWTClaimsSet claims = jwtProcessor.process(token, null);

				// Validate issuer
				if (!expectedIssuer.equals(claims.getIssuer())) {
					log.warn("JWT issuer mismatch: {}", claims.getIssuer());
					filterChain.doFilter(request, response);
					return;
				}

				String clerkUserId = claims.getSubject();
				String email = (String) claims.getClaim("email");

				// Store claims in request for later use
				request.setAttribute("clerkUserId", clerkUserId);
				request.setAttribute("email", email);
				request.setAttribute("clerkClaims", claims);

				ClerkPrincipal principal = new ClerkPrincipal(clerkUserId, email, claims);
				UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(principal, null,
						List.of(new SimpleGrantedAuthority("ROLE_USER")));
				SecurityContextHolder.getContext().setAuthentication(auth);

				log.debug("Authenticated Clerk user: {}", clerkUserId);
			} catch (Exception e) {
				log.warn("JWT validation failed: {}", e.getMessage());
				SecurityContextHolder.clearContext();
			}
		}
		filterChain.doFilter(request, response);
	}

	private String extractToken(HttpServletRequest request) {
		String header = request.getHeader("Authorization");
		if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
			return header.substring(7);
		}
		return null;
	}
}