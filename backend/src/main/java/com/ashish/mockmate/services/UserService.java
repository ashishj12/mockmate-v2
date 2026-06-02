package com.ashish.mockmate.services;

import com.ashish.mockmate.dto.request.OnboardingRequest;
import com.ashish.mockmate.dto.response.UserResponse;
import com.ashish.mockmate.security.ClerkPrincipal;

public interface UserService {
	UserResponse getOrCreateUser(ClerkPrincipal principal);

	UserResponse updateProfile(ClerkPrincipal principal, OnboardingRequest request);

	UserResponse getProfile(ClerkPrincipal principal);

	boolean isOnboarded(ClerkPrincipal principal);
}
