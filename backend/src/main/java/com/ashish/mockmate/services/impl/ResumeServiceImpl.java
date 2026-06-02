package com.ashish.mockmate.services.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ashish.mockmate.dto.request.AtsAnalysisRequest;
import com.ashish.mockmate.dto.request.ResumeSaveRequest;
import com.ashish.mockmate.dto.response.AtsAnalysisResponse;
import com.ashish.mockmate.dto.response.ResumeResponse;
import com.ashish.mockmate.exception.BadRequestException;
import com.ashish.mockmate.exception.ResourceNotFoundException;
import com.ashish.mockmate.models.AtsAnalysis;
import com.ashish.mockmate.models.Resume;
import com.ashish.mockmate.models.User;
import com.ashish.mockmate.repositories.AtsAnalysisRepository;
import com.ashish.mockmate.repositories.ResumeRepository;
import com.ashish.mockmate.repositories.UserRepository;
import com.ashish.mockmate.security.ClerkPrincipal;
import com.ashish.mockmate.services.GeminiService;
import com.ashish.mockmate.services.ResumeService;
import com.ashish.mockmate.util.EntityMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeServiceImpl implements ResumeService {

	private final UserRepository userRepository;
	private final ResumeRepository resumeRepository;
	private final AtsAnalysisRepository atsAnalysisRepository;
	private final GeminiService geminiService;
	private final ObjectMapper objectMapper;

	@Override
	@Transactional(readOnly = true)
	public ResumeResponse getResume(ClerkPrincipal principal) {
		User user = getUser(principal);
		Resume resume = resumeRepository.findByUserId(user.getId()).orElse(null);
		if (resume == null)
			return null;
		return EntityMapper.toResumeResponse(resume);
	}

	@Override
	@Transactional
	public ResumeResponse saveResume(ClerkPrincipal principal, ResumeSaveRequest request) {
		User user = getUser(principal);
		Resume resume = resumeRepository.findByUserId(user.getId())
				.orElseGet(() -> Resume.builder().user(user).build());

		resume.setContent(request.getContent() != null ? request.getContent() : "");
		Resume saved = resumeRepository.save(resume);
		log.info("Saved resume for user: {}", user.getId());
		return EntityMapper.toResumeResponse(saved);
	}

	@Override
	@Transactional
	public ResumeResponse generateResume(ClerkPrincipal principal) {
		User user = getUser(principal);

		if (user.getIndustry() == null) {
			throw new BadRequestException("Please complete onboarding before generating a resume");
		}

		String skillsStr = user.getSkills() != null ? String.join(", ", user.getSkills()) : "Not specified";
		String expStr = user.getExperience() != null ? user.getExperience().toString() : "0";
		String bioStr = user.getBio() != null ? user.getBio() : "A motivated professional";

		String content = geminiService.generateResume(bioStr, expStr, skillsStr, user.getIndustry());

		Resume resume = resumeRepository.findByUserId(user.getId())
				.orElseGet(() -> Resume.builder().user(user).build());
		resume.setContent(content);
		Resume saved = resumeRepository.save(resume);

		return EntityMapper.toResumeResponse(saved);
	}

	@Override
	@Transactional
	public AtsAnalysisResponse analyzeAts(ClerkPrincipal principal, AtsAnalysisRequest request) {
		User user = getUser(principal);
		Resume resume = resumeRepository.findByUserId(user.getId())
				.orElseThrow(() -> new ResourceNotFoundException("No resume found. Please create a resume first."));

		if (resume.getContent() == null || resume.getContent().isBlank()) {
			throw new BadRequestException("Resume content is empty. Please add content before analyzing.");
		}

		String aiResponse = geminiService.analyzeResumeForAts(resume.getContent(), request.getJobDescription(),
				request.getJobTitle(), request.getCompanyName());

		try {
			// Clean response - remove markdown code fences if present
			String cleaned = aiResponse.trim().replaceAll("```json\\s*", "").replaceAll("```\\s*", "");

			Map<String, Object> analysisData = objectMapper.readValue(cleaned,
					new TypeReference<Map<String, Object>>() {
					});

			// FIX BUG 2 & 3: Use repository delete instead of accessing lazy field.
			// orphanRemoval on Resume.atsAnalysis handles cascade, but we also
			// explicitly delete to ensure immediate flush before re-save.
			atsAnalysisRepository.findByResumeId(resume.getId())
					.ifPresent(existing -> atsAnalysisRepository.deleteById(existing.getId()));
			atsAnalysisRepository.flush();

			AtsAnalysis analysis = AtsAnalysis.builder().resume(resume)
					.overallScore(getInt(analysisData, "overallScore"))
					.keywordMatchScore(getInt(analysisData, "keywordMatchScore"))
					.formatScore(getInt(analysisData, "formatScore")).skillsScore(getInt(analysisData, "skillsScore"))
					.experienceScore(getInt(analysisData, "experienceScore"))
					.matchedKeywords(toStringArray(analysisData, "matchedKeywords"))
					.missingKeywords(toStringArray(analysisData, "missingKeywords"))
					.jobDescription(request.getJobDescription()).jobTitle(request.getJobTitle())
					.companyName(request.getCompanyName()).improvements(getListOfMaps(analysisData, "improvements"))
					.suggestions(getListOfMaps(analysisData, "suggestions"))
					.totalKeywords(getInt(analysisData, "totalKeywords"))
					.matchedCount(getInt(analysisData, "matchedCount")).build();

			AtsAnalysis saved = atsAnalysisRepository.save(analysis);
			return EntityMapper.toAtsAnalysisResponse(saved);

		} catch (Exception e) {
			log.error("Failed to parse ATS analysis response: {}", e.getMessage());
			throw new RuntimeException("Failed to parse AI analysis. Please try again.");
		}
	}

	// ---- helpers ----

	private User getUser(ClerkPrincipal principal) {
		return userRepository.findByClerkUserId(principal.getClerkUserId())
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
	}

	private int getInt(Map<String, Object> map, String key) {
		Object val = map.get(key);
		if (val instanceof Integer i)
			return i;
		if (val instanceof Number n)
			return n.intValue();
		return 0;
	}

	@SuppressWarnings("unchecked")
	private String[] toStringArray(Map<String, Object> map, String key) {
		Object val = map.get(key);
		if (val instanceof List<?> list) {
			return list.stream().map(Object::toString).toArray(String[]::new);
		}
		return new String[] {};
	}

	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> getListOfMaps(Map<String, Object> map, String key) {
		Object val = map.get(key);
		if (val instanceof List<?> list) {
			return (List<Map<String, Object>>) list;
		}
		return List.of();
	}
}
