package com.ashish.mockmate.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OnboardingRequest {
	@NotBlank(message = "Industy is required")
	private String industry;
	private String bio;
	private Integer experience;
	private List<String> skills;

}
