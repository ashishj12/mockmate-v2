package com.ashish.mockmate.services;

import com.ashish.mockmate.dto.request.AtsAnalysisRequest;
import com.ashish.mockmate.dto.request.ResumeSaveRequest;
import com.ashish.mockmate.dto.response.AtsAnalysisResponse;
import com.ashish.mockmate.dto.response.ResumeResponse;
import com.ashish.mockmate.security.ClerkPrincipal;

public interface ResumeService {
	ResumeResponse getResume(ClerkPrincipal principal);

	ResumeResponse saveResume(ClerkPrincipal principal, ResumeSaveRequest request);

	ResumeResponse generateResume(ClerkPrincipal principal);

	AtsAnalysisResponse analyzeAts(ClerkPrincipal principal, AtsAnalysisRequest request);
}
