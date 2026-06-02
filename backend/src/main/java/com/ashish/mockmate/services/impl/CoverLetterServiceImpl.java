package com.ashish.mockmate.services.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ashish.mockmate.dto.request.CoverLetterRequest;
import com.ashish.mockmate.dto.response.CoverLetterResponse;
import com.ashish.mockmate.exception.ResourceNotFoundException;
import com.ashish.mockmate.models.CoverLetter;
import com.ashish.mockmate.models.User;
import com.ashish.mockmate.repositories.CoverLetterRepository;
import com.ashish.mockmate.repositories.UserRepository;
import com.ashish.mockmate.security.ClerkPrincipal;
import com.ashish.mockmate.services.CoverLetterService;
import com.ashish.mockmate.services.GeminiService;
import com.ashish.mockmate.util.EntityMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoverLetterServiceImpl implements CoverLetterService {

	private final UserRepository userRepository;
	private final CoverLetterRepository coverLetterRepository;
	private final GeminiService geminiService;

	@Override
	@Transactional(readOnly = true)
	public List<CoverLetterResponse> getAllCoverLetters(ClerkPrincipal principal) {
		User user = getUser(principal);
		return coverLetterRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
				.map(EntityMapper::toCoverLetterResponse).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public CoverLetterResponse getCoverLetter(ClerkPrincipal principal, String id) {
		User user = getUser(principal);
		CoverLetter letter = coverLetterRepository.findByIdAndUserId(id, user.getId())
				.orElseThrow(() -> new ResourceNotFoundException("Cover letter not found"));
		return EntityMapper.toCoverLetterResponse(letter);
	}

	@Override
	@Transactional
	public CoverLetterResponse generateCoverLetter(ClerkPrincipal principal, CoverLetterRequest request) {
		User user = getUser(principal);

		String skills = user.getSkills() != null ? String.join(", ", user.getSkills()) : "various skills";
		String exp = user.getExperience() != null ? user.getExperience().toString() : "0";
		String bio = user.getBio() != null ? user.getBio() : "A motivated professional";

		String content = geminiService.generateCoverLetter(bio, skills, exp, request.getCompanyName(),
				request.getJobTitle(), request.getJobDescription());

		CoverLetter letter = CoverLetter.builder().user(user).content(content).companyName(request.getCompanyName())
				.jobTitle(request.getJobTitle()).jobDescription(request.getJobDescription()).status("completed")
				.build();

		CoverLetter saved = coverLetterRepository.save(letter);
		log.info("Generated cover letter {} for user {}", saved.getId(), user.getId());
		return EntityMapper.toCoverLetterResponse(saved);
	}

	@Override
	@Transactional
	public void deleteCoverLetter(ClerkPrincipal principal, String id) {
		User user = getUser(principal);
		if (!coverLetterRepository.findByIdAndUserId(id, user.getId()).isPresent()) {
			throw new ResourceNotFoundException("Cover letter not found");
		}
		coverLetterRepository.deleteByIdAndUserId(id, user.getId());
		log.info("Deleted cover letter {} for user {}", id, user.getId());
	}

	private User getUser(ClerkPrincipal principal) {
		return userRepository.findByClerkUserId(principal.getClerkUserId())
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
	}
}
