package com.ashish.mockmate.services.impl;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ashish.mockmate.dto.request.AssessmentSubmitRequest;
import com.ashish.mockmate.dto.response.AssessmentResponse;
import com.ashish.mockmate.exception.ResourceNotFoundException;
import com.ashish.mockmate.models.Assessment;
import com.ashish.mockmate.models.User;
import com.ashish.mockmate.repositories.AssessmentRepository;
import com.ashish.mockmate.repositories.UserRepository;
import com.ashish.mockmate.security.ClerkPrincipal;
import com.ashish.mockmate.services.AssessmentService;
import com.ashish.mockmate.services.GeminiService;
import com.ashish.mockmate.util.EntityMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssessmentServiceImpl implements AssessmentService {

	private final UserRepository userRepository;
	private final AssessmentRepository assessmentRepository;
	private final GeminiService geminiService;
	private final ObjectMapper objectMapper;

	@Override
	@Transactional(readOnly = true)
	public List<Map<String, Object>> generateQuestions(ClerkPrincipal principal, String category, int count) {
		User user = getUser(principal);
		String industry = user.getIndustry() != null ? user.getIndustry() : "Software Engineering";
		String skills = user.getSkills() != null ? String.join(", ", user.getSkills()) : "Java, Spring Boot";

		int questionCount = Math.min(Math.max(count, 5), 20); // clamp between 5 and 20

		String aiResponse = geminiService.generateInterviewQuestions(category, industry, skills, questionCount);

		try {
			String cleaned = aiResponse.trim().replaceAll("```json\\s*", "").replaceAll("```\\s*", "");

			return objectMapper.readValue(cleaned, new TypeReference<List<Map<String, Object>>>() {
			});
		} catch (Exception e) {
			log.error("Failed to parse questions: {}", e.getMessage());
			throw new RuntimeException("Failed to generate questions. Please try again.");
		}
	}

	@Override
	@Transactional
	public AssessmentResponse submitAssessment(ClerkPrincipal principal, AssessmentSubmitRequest request) {
		User user = getUser(principal);

		// Calculate score from submitted answers
		List<Map<String, Object>> questions = request.getQuestions();
		long correctCount = questions.stream().filter(q -> Boolean.TRUE.equals(q.get("isCorrect"))).count();
		double score = questions.isEmpty() ? 0.0 : (double) correctCount / questions.size() * 100.0;

		// Get incorrect questions for improvement tips
		List<Map<String, Object>> incorrect = questions.stream().filter(q -> !Boolean.TRUE.equals(q.get("isCorrect")))
				.collect(Collectors.toList());

		// Generate AI improvement tip
		String tip = null;
		try {
			tip = geminiService.generateImprovementTip(request.getCategory(), incorrect, score);
		} catch (Exception e) {
			log.warn("Could not generate improvement tip: {}", e.getMessage());
			tip = "Keep practicing to improve your score!";
		}

		Assessment assessment = Assessment.builder().user(user).quizScore(score).questions(questions)
				.category(request.getCategory()).improvementTip(tip).build();

		Assessment saved = assessmentRepository.save(assessment);
		log.info("Assessment saved for user {} with score {}", user.getId(), score);
		return EntityMapper.toAssessmentResponse(saved);
	}

	@Override
	@Transactional(readOnly = true)
	public List<AssessmentResponse> getAssessmentHistory(ClerkPrincipal principal) {
		User user = getUser(principal);
		return assessmentRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
				.map(EntityMapper::toAssessmentResponse).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public AssessmentResponse getAssessment(ClerkPrincipal principal, String id) {
		User user = getUser(principal);
		// FIX BUG 4: Use findByIdAndUserId - single query, zero lazy traversal,
		// enforces ownership at the DB level.
		Assessment assessment = assessmentRepository.findByIdAndUserId(UUID.fromString(id), user.getId())
				.orElseThrow(() -> new ResourceNotFoundException("Assessment not found"));
		return EntityMapper.toAssessmentResponse(assessment);
	}

	private User getUser(ClerkPrincipal principal) {
		return userRepository.findByClerkUserId(principal.getClerkUserId())
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
	}
}
