package com.ashish.mockmate.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AtsAnalysisResponse {
	private String id;
	private Integer overallScore;
	private Integer keywordMatchScore;
	private Integer formatScore;
	private Integer skillsScore;
	private Integer experienceScore;
	private String[] matchedKeywords;
	private String[] missingKeywords;
	private String jobDescription;
	private String jobTitle;
	private String companyName;
	private List<Map<String, Object>> improvements;
	private List<Map<String, Object>> suggestions;
	private Integer totalKeywords;
	private Integer matchedCount;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}