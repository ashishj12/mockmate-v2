package com.ashish.mockmate.services.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ashish.mockmate.dto.request.OnboardingRequest;
import com.ashish.mockmate.dto.response.UserResponse;
import com.ashish.mockmate.exception.ResourceNotFoundException;
import com.ashish.mockmate.models.User;
import com.ashish.mockmate.repositories.UserRepository;
import com.ashish.mockmate.security.ClerkPrincipal;
import com.ashish.mockmate.services.UserService;
import com.ashish.mockmate.util.EntityMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;

	@Override
	@Transactional
	public UserResponse getOrCreateUser(ClerkPrincipal principal) {
		return userRepository.findByClerkUserId(principal.getClerkUserId()).map(EntityMapper::toUserResponse)
				.orElseGet(() -> {
					log.info("Creating new user for clearkId: {}", principal.getClerkUserId());

					User newUser = User.builder().clerkUserId(principal.getClerkUserId())
							.email(principal.getEmail() != null ? principal.getEmail() : "")
							.name(principal.getFullName()).imageUrl(principal.getImageUrl()).build();
					User saved = userRepository.save(newUser);
					return EntityMapper.toUserResponse(saved);
				});
	}

	@Override
	@Transactional
	public UserResponse updateProfile(ClerkPrincipal principal, OnboardingRequest request) {
		User user = userRepository.findByClerkUserId(principal.getClerkUserId())
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		user.setIndustry(request.getIndustry());
		if (request.getBio() != null)
			user.setBio(request.getBio());
		if (request.getExperience() != null)
			user.setExperience(request.getExperience());
		if (request.getSkills() != null) {
			user.setSkills(request.getSkills().toArray(new String[0]));
		}

		User saved = userRepository.save(user);
		log.info("Updated profile for user: {}", saved.getId());
		return EntityMapper.toUserResponse(saved);
	}

	@Override
	@Transactional(readOnly = true)
	public UserResponse getProfile(ClerkPrincipal principal) {
		User user = userRepository.findByClerkUserId(principal.getClerkUserId())
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
		return EntityMapper.toUserResponse(user);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean isOnboarded(ClerkPrincipal principal) {
		return userRepository.findByClerkUserId(principal.getClerkUserId())
				.map(u -> u.getIndustry() != null && !u.getIndustry().isBlank()).orElse(false);
	}
}
