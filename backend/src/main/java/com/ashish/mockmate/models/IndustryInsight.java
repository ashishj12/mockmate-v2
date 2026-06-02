package com.ashish.mockmate.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.annotations.Type;

import io.hypersistence.utils.hibernate.type.array.StringArrayType;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "industry_insights", indexes = @Index(name = "idx_industry_insights_industry", columnList = "industry"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IndustryInsight {

	@Id
	@Column(name = "id", nullable = false)
	private String id;

	@Column(name = "industry", nullable = false, unique = true)
	private String industry;

	@OneToMany(mappedBy = "industryInsight", fetch = FetchType.LAZY)
	@Builder.Default
	private List<User> users = new ArrayList<>();

	@Type(JsonBinaryType.class)
	@Column(name = "salary_ranges", columnDefinition = "jsonb")
	@Builder.Default
	private List<Map<String, Object>> salaryRanges = new ArrayList<>();

	@Column(name = "growth_rate", nullable = false)
	private Double growthRate;

	@Column(name = "demand_level", nullable = false)
	private String demandLevel;

	@Type(StringArrayType.class)
	@Column(name = "top_skills", columnDefinition = "text[]")
	@Builder.Default
	private String[] topSkills = new String[] {};

	@Column(name = "market_outlook", nullable = false)
	private String marketOutlook;

	@Type(StringArrayType.class)
	@Column(name = "key_trends", columnDefinition = "text[]")
	@Builder.Default
	private String[] keyTrends = new String[] {};

	@Type(StringArrayType.class)
	@Column(name = "recommended_skills", columnDefinition = "text[]")
	@Builder.Default
	private String[] recommendedSkills = new String[] {};

	@Column(name = "last_updated", nullable = false)
	private LocalDateTime lastUpdated;

	@Column(name = "next_update", nullable = false)
	private LocalDateTime nextUpdate;

	@PrePersist
	protected void onCreate() {
		if (id == null) {
			id = "ci_" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 20);
		}
		if (lastUpdated == null)
			lastUpdated = LocalDateTime.now();
		if (nextUpdate == null)
			nextUpdate = LocalDateTime.now().plusWeeks(1);
	}
}