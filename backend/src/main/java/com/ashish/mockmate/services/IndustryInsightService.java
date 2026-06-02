package com.ashish.mockmate.services;

import com.ashish.mockmate.dto.response.IndustryInsightResponse;
import com.ashish.mockmate.security.ClerkPrincipal;

public interface IndustryInsightService {
	IndustryInsightResponse getInsightsForUser(ClerkPrincipal principal);

	IndustryInsightResponse getInsightsByIndustry(String industry);
}