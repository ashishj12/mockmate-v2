package com.ashish.mockmate.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResumeResponse {
	private UUID id;
	private String content;
	private AtsAnalysisResponse atsAnalysis;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}