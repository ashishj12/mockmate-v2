package com.ashish.mockmate.security;

import java.security.Principal;

import com.nimbusds.jwt.JWTClaimsSet;

import lombok.Getter;

@Getter
public class ClerkPrincipal implements Principal {

	private final String clerkUserId;
	private final String email;
	private final JWTClaimsSet claims;

	public ClerkPrincipal(String clerkUserId, String email, JWTClaimsSet claims) {
		this.clerkUserId = clerkUserId;
		this.email = email;
		this.claims = claims;
	}

	public String getName() {
		return clerkUserId;
	}

	public String getFirstName() {
		try {
			return (String) claims.getClaim("first_name");
		} catch (Exception e) {
			return null;
		}
	}

	public String getLastName() {
		try {
			return (String) claims.getClaim("last_name");
		} catch (Exception e) {
			return null;
		}
	}

	public String getFullName() {
		String first = getFirstName();
		String last = getLastName();
		if (first != null && last != null)
			return first + " " + last;
		if (first != null)
			return first;
		return null;
	}

	public String getImageUrl() {
		try {
			return (String) claims.getClaim("image_url");
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public String toString() {
		return "ClerkPrincipal{clerkUserId='" + clerkUserId + "', email='" + email + "'}";
	}
}