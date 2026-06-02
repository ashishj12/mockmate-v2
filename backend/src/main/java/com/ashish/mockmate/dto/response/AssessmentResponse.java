package com.ashish.mockmate.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssessmentResponse {
	private UUID id;
	private Double quizScore;
	private List<Map<String, Object>> questions;
	private String category;
	private String improvementTip;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
