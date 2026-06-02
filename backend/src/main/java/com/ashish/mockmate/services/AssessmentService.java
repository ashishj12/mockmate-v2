package com.ashish.mockmate.services;

import java.util.List;
import java.util.Map;

import com.ashish.mockmate.dto.request.AssessmentSubmitRequest;
import com.ashish.mockmate.dto.response.AssessmentResponse;
import com.ashish.mockmate.security.ClerkPrincipal;

public interface AssessmentService {
	List<Map<String, Object>> generateQuestions(ClerkPrincipal principal, String category, int count);

	AssessmentResponse submitAssessment(ClerkPrincipal principal, AssessmentSubmitRequest request);

	List<AssessmentResponse> getAssessmentHistory(ClerkPrincipal principal);

	AssessmentResponse getAssessment(ClerkPrincipal principal, String id);
}
