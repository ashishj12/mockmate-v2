package com.ashish.mockmate.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ashish.mockmate.dto.request.AtsAnalysisRequest;
import com.ashish.mockmate.dto.request.ResumeSaveRequest;
import com.ashish.mockmate.dto.response.ApiResponse;
import com.ashish.mockmate.dto.response.AtsAnalysisResponse;
import com.ashish.mockmate.dto.response.ResumeResponse;
import com.ashish.mockmate.security.ClerkPrincipal;
import com.ashish.mockmate.services.ResumeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/resume")
@RequiredArgsConstructor
public class ResumeController {

	private final ResumeService resumeService;

	@GetMapping
	public ResponseEntity<ApiResponse<ResumeResponse>> getResume(@AuthenticationPrincipal ClerkPrincipal principal) {
		ResumeResponse resume = resumeService.getResume(principal);
		return ResponseEntity.ok(ApiResponse.success(resume));
	}

	@PutMapping
	public ResponseEntity<ApiResponse<ResumeResponse>> saveResume(@AuthenticationPrincipal ClerkPrincipal principal,
			@RequestBody ResumeSaveRequest request) {
		ResumeResponse resume = resumeService.saveResume(principal, request);
		return ResponseEntity.ok(ApiResponse.success("Resume saved", resume));
	}

	@PostMapping("/generate")
	public ResponseEntity<ApiResponse<ResumeResponse>> generateResume(
			@AuthenticationPrincipal ClerkPrincipal principal) {
		ResumeResponse resume = resumeService.generateResume(principal);
		return ResponseEntity.ok(ApiResponse.success("Resume generated", resume));
	}

	@PostMapping("/ats-analysis")
	public ResponseEntity<ApiResponse<AtsAnalysisResponse>> analyzeAts(
			@AuthenticationPrincipal ClerkPrincipal principal, @Valid @RequestBody AtsAnalysisRequest request) {
		AtsAnalysisResponse analysis = resumeService.analyzeAts(principal, request);
		return ResponseEntity.ok(ApiResponse.success("ATS analysis complete", analysis));
	}
}