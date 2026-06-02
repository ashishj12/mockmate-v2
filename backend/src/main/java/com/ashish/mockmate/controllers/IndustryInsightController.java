package com.ashish.mockmate.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ashish.mockmate.dto.response.ApiResponse;
import com.ashish.mockmate.dto.response.IndustryInsightResponse;
import com.ashish.mockmate.security.ClerkPrincipal;
import com.ashish.mockmate.services.IndustryInsightService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/v1/industry-insights")
@RequiredArgsConstructor
public class IndustryInsightController {

	private final IndustryInsightService industryInsightService;

	/**
	 * Get insights for the currently logged-in user's industry.
	 */
	@GetMapping("/me")
	public ResponseEntity<ApiResponse<IndustryInsightResponse>> getMyInsights(
			@AuthenticationPrincipal ClerkPrincipal principal) {
		IndustryInsightResponse insights = industryInsightService.getInsightsForUser(principal);
		return ResponseEntity.ok(ApiResponse.success(insights));
	}

	/**
	 * Get insights for any specific industry (e.g. during onboarding preview).
	 */
	@GetMapping("/{industry}")
	public ResponseEntity<ApiResponse<IndustryInsightResponse>> getInsightsByIndustry(@PathVariable String industry) {
		IndustryInsightResponse insights = industryInsightService.getInsightsByIndustry(industry);
		return ResponseEntity.ok(ApiResponse.success(insights));
	}
}
