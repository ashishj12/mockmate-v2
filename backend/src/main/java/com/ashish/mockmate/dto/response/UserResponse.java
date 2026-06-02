package com.ashish.mockmate.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {
	private UUID id;
	private String clerkUserId;
	private String email;
	private String name;
	private String imageUrl;
	private String industry;
	private String bio;
	private Integer experience;
	private String[] skills;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}