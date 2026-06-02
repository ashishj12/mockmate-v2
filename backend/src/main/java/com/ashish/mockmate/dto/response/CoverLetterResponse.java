package com.ashish.mockmate.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CoverLetterResponse {
	private String id;
	private String content;
	private String jobDescription;
	private String companyName;
	private String jobTitle;
	private String status;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
