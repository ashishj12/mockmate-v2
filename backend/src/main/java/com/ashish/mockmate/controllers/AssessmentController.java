package com.ashish.mockmate.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ashish.mockmate.dto.request.AssessmentSubmitRequest;
import com.ashish.mockmate.dto.response.ApiResponse;
import com.ashish.mockmate.dto.response.AssessmentResponse;
import com.ashish.mockmate.security.ClerkPrincipal;
import com.ashish.mockmate.services.AssessmentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/v1/assessments")
@RequiredArgsConstructor
public class AssessmentController {

	private final AssessmentService assessmentService;

	/**
	 * Generate a fresh set of interview questions (not persisted until submitted).
	 * category = "technical" | "behavioral" count = number of questions (5-20),
	 * defaults to 10
	 */
	@GetMapping("/generate-questions")
	public ResponseEntity<ApiResponse<List<Map<String, Object>>>> generateQuestions(
			@AuthenticationPrincipal ClerkPrincipal principal,
			@RequestParam(defaultValue = "technical") String category, @RequestParam(defaultValue = "10") int count) {
		List<Map<String, Object>> questions = assessmentService.generateQuestions(principal, category, count);
		return ResponseEntity.ok(ApiResponse.success(questions));
	}

	/**
	 * Submit completed quiz with user answers - calculates score and persists.
	 */
	@PostMapping("/submit")
	public ResponseEntity<ApiResponse<AssessmentResponse>> submitAssessment(
			@AuthenticationPrincipal ClerkPrincipal principal, @Valid @RequestBody AssessmentSubmitRequest request) {
		AssessmentResponse assessment = assessmentService.submitAssessment(principal, request);
		return ResponseEntity.ok(ApiResponse.success("Assessment submitted", assessment));
	}

	/**
	 * Get full assessment history for the current user.
	 */
	@GetMapping
	public ResponseEntity<ApiResponse<List<AssessmentResponse>>> getHistory(
			@AuthenticationPrincipal ClerkPrincipal principal) {
		List<AssessmentResponse> history = assessmentService.getAssessmentHistory(principal);
		return ResponseEntity.ok(ApiResponse.success(history));
	}

	/**
	 * Get a single assessment by ID.
	 */
	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<AssessmentResponse>> getAssessment(
			@AuthenticationPrincipal ClerkPrincipal principal, @PathVariable String id) {
		AssessmentResponse assessment = assessmentService.getAssessment(principal, id);
		return ResponseEntity.ok(ApiResponse.success(assessment));
	}
}
