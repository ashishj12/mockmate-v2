package com.ashish.mockmate.services;

import java.util.List;

import com.ashish.mockmate.dto.request.CoverLetterRequest;
import com.ashish.mockmate.dto.response.CoverLetterResponse;
import com.ashish.mockmate.security.ClerkPrincipal;

public interface CoverLetterService {
	List<CoverLetterResponse> getAllCoverLetters(ClerkPrincipal principal);

	CoverLetterResponse getCoverLetter(ClerkPrincipal principal, String id);

	CoverLetterResponse generateCoverLetter(ClerkPrincipal principal, CoverLetterRequest request);

	void deleteCoverLetter(ClerkPrincipal principal, String id);
}
