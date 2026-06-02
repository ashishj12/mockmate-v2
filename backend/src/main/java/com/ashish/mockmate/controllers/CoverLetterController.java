package com.ashish.mockmate.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ashish.mockmate.dto.request.CoverLetterRequest;
import com.ashish.mockmate.dto.response.ApiResponse;
import com.ashish.mockmate.dto.response.CoverLetterResponse;
import com.ashish.mockmate.security.ClerkPrincipal;
import com.ashish.mockmate.services.CoverLetterService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/cover-letters")
@RequiredArgsConstructor
public class CoverLetterController {

	private final CoverLetterService coverLetterService;

	@GetMapping
	public ResponseEntity<ApiResponse<List<CoverLetterResponse>>> getAllCoverLetters(
			@AuthenticationPrincipal ClerkPrincipal principal) {
		List<CoverLetterResponse> letters = coverLetterService.getAllCoverLetters(principal);
		return ResponseEntity.ok(ApiResponse.success(letters));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<CoverLetterResponse>> getCoverLetter(
			@AuthenticationPrincipal ClerkPrincipal principal, @PathVariable String id) {
		CoverLetterResponse letter = coverLetterService.getCoverLetter(principal, id);
		return ResponseEntity.ok(ApiResponse.success(letter));
	}

	@PostMapping("/generate")
	public ResponseEntity<ApiResponse<CoverLetterResponse>> generateCoverLetter(
			@AuthenticationPrincipal ClerkPrincipal principal, @Valid @RequestBody CoverLetterRequest request) {
		CoverLetterResponse letter = coverLetterService.generateCoverLetter(principal, request);
		return ResponseEntity.ok(ApiResponse.success("Cover letter generated", letter));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<Void>> deleteCoverLetter(@AuthenticationPrincipal ClerkPrincipal principal,
			@PathVariable String id) {
		coverLetterService.deleteCoverLetter(principal, id);
		return ResponseEntity.ok(ApiResponse.success("Cover letter deleted", null));
	}
}
