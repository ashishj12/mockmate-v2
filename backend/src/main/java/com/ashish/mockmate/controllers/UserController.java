package com.ashish.mockmate.controllers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ashish.mockmate.dto.request.OnboardingRequest;
import com.ashish.mockmate.dto.response.ApiResponse;
import com.ashish.mockmate.dto.response.UserResponse;
import com.ashish.mockmate.security.ClerkPrincipal;
import com.ashish.mockmate.services.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	/**
	 * Called on every page load by the frontend to ensure user exists in DB. Also
	 * used for first-time user creation after Clerk sign-up.
	 */
	@PostMapping("/sync")
	public ResponseEntity<ApiResponse<UserResponse>> syncUser(@AuthenticationPrincipal ClerkPrincipal principal) {
		UserResponse user = userService.getOrCreateUser(principal);
		return ResponseEntity.ok(ApiResponse.success("User synced", user));
	}

	@GetMapping("/me")
	public ResponseEntity<ApiResponse<UserResponse>> getProfile(@AuthenticationPrincipal ClerkPrincipal principal) {
		UserResponse user = userService.getProfile(principal);
		return ResponseEntity.ok(ApiResponse.success(user));
	}

	@PutMapping("/onboarding")
	public ResponseEntity<ApiResponse<UserResponse>> completeOnboarding(
			@AuthenticationPrincipal ClerkPrincipal principal, @Valid @RequestBody OnboardingRequest request) {
		UserResponse user = userService.updateProfile(principal, request);
		return ResponseEntity.ok(ApiResponse.success("Onboarding completed", user));
	}

	@PatchMapping("/profile")
	public ResponseEntity<ApiResponse<UserResponse>> updateProfile(@AuthenticationPrincipal ClerkPrincipal principal,
			@Valid @RequestBody OnboardingRequest request) {
		UserResponse user = userService.updateProfile(principal, request);
		return ResponseEntity.ok(ApiResponse.success("Profile updated", user));
	}

	@GetMapping("/onboarding-status")
	public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkOnboarding(
			@AuthenticationPrincipal ClerkPrincipal principal) {
		boolean onboarded = userService.isOnboarded(principal);
		return ResponseEntity.ok(ApiResponse.success(Map.of("isOnboarded", onboarded)));
	}
}