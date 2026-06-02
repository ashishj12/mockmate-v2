package com.ashish.mockmate.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AtsAnalysisRequest {
	@NotBlank(message = "Job description is required")
	private String jobDescription;
	private String jobTitle;
	private String companyName;
}
