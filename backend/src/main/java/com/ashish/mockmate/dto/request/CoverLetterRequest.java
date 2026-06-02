package com.ashish.mockmate.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CoverLetterRequest {
	@NotBlank(message = "Company name is required")
	private String companyName;

	@NotBlank(message = "Job titile is required")
	private String jobTitle;

	private String jobDescription;
}
