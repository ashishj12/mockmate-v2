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
public class IndustryInsightResponse {
	private String id;
	private String industry;
	private List<Map<String, Object>> salaryRanges;
	private Double growthRate;
	private String demandLevel;
	private String[] topSkills;
	private String marketOutlook;
	private String[] keyTrends;
	private String[] recommendedSkills;
	private LocalDateTime lastUpdated;
	private LocalDateTime nextUpdate;
}
