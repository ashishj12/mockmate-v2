package com.ashish.mockmate.dto.request;

import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssessmentSubmitRequest {
	@NotBlank(message = "Category is required")
	private String category;

	@NotNull(message = "Questions are required")
	private List<Map<String, Object>> questions;
}
