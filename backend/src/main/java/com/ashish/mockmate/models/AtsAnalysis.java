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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ats_analyses", indexes = @Index(name = "idx_ats_analyses_resume_id", columnList = "resume_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtsAnalysis {

	@Id
	@Column(name = "id", nullable = false)
	private String id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "resume_id", nullable = false, unique = true)
	private Resume resume;

	@Column(name = "overall_score", nullable = false)
	private Integer overallScore;

	@Column(name = "keyword_match_score", nullable = false)
	private Integer keywordMatchScore;

	@Column(name = "format_score", nullable = false)
	private Integer formatScore;

	@Column(name = "skills_score", nullable = false)
	private Integer skillsScore;

	@Column(name = "experience_score", nullable = false)
	private Integer experienceScore;

	@Type(StringArrayType.class)
	@Column(name = "matched_keywords", columnDefinition = "text[]")
	@Builder.Default
	private String[] matchedKeywords = new String[] {};

	@Type(StringArrayType.class)
	@Column(name = "missing_keywords", columnDefinition = "text[]")
	@Builder.Default
	private String[] missingKeywords = new String[] {};

	@Column(name = "job_description", columnDefinition = "TEXT", nullable = false)
	private String jobDescription;

	@Column(name = "job_title")
	private String jobTitle;

	@Column(name = "company_name")
	private String companyName;

	@Type(JsonBinaryType.class)
	@Column(name = "improvements", columnDefinition = "jsonb")
	@Builder.Default
	private List<Map<String, Object>> improvements = new ArrayList<>();

	@Type(JsonBinaryType.class)
	@Column(name = "suggestions", columnDefinition = "jsonb")
	@Builder.Default
	private List<Map<String, Object>> suggestions = new ArrayList<>();

	@Column(name = "total_keywords", nullable = false)
	private Integer totalKeywords;

	@Column(name = "matched_count", nullable = false)
	private Integer matchedCount;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@PrePersist
	protected void onCreate() {
		if (id == null) {
			id = "ca_" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 20);
		}
		createdAt = LocalDateTime.now();
		updatedAt = LocalDateTime.now();
	}

	@PreUpdate
	protected void onUpdate() {
		updatedAt = LocalDateTime.now();
	}
}