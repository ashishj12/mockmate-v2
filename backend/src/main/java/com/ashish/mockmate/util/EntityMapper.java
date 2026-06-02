package com.ashish.mockmate.util;

import com.ashish.mockmate.dto.response.AssessmentResponse;
import com.ashish.mockmate.dto.response.AtsAnalysisResponse;
import com.ashish.mockmate.dto.response.CoverLetterResponse;
import com.ashish.mockmate.dto.response.IndustryInsightResponse;
import com.ashish.mockmate.dto.response.ResumeResponse;
import com.ashish.mockmate.dto.response.UserResponse;
import com.ashish.mockmate.models.Assessment;
import com.ashish.mockmate.models.AtsAnalysis;
import com.ashish.mockmate.models.CoverLetter;
import com.ashish.mockmate.models.IndustryInsight;
import com.ashish.mockmate.models.Resume;
import com.ashish.mockmate.models.User;

public class EntityMapper {

	private EntityMapper() {
	} // utility class

	public static UserResponse toUserResponse(User user) {
		return UserResponse.builder().id(user.getId()).clerkUserId(user.getClerkUserId()).email(user.getEmail())
				.name(user.getName()).imageUrl(user.getImageUrl()).industry(user.getIndustry()).bio(user.getBio())
				.experience(user.getExperience()).skills(user.getSkills()).createdAt(user.getCreatedAt())
				.updatedAt(user.getUpdatedAt()).build();
	}

	public static AssessmentResponse toAssessmentResponse(Assessment assessment) {
		return AssessmentResponse.builder().id(assessment.getId()).quizScore(assessment.getQuizScore())
				.questions(assessment.getQuestions()).category(assessment.getCategory())
				.improvementTip(assessment.getImprovementTip()).createdAt(assessment.getCreatedAt())
				.updatedAt(assessment.getUpdatedAt()).build();
	}

	public static ResumeResponse toResumeResponse(Resume resume) {
		AtsAnalysisResponse atsResponse = null;
		if (resume.getAtsAnalysis() != null) {
			atsResponse = toAtsAnalysisResponse(resume.getAtsAnalysis());
		}
		return ResumeResponse.builder().id(resume.getId()).content(resume.getContent()).atsAnalysis(atsResponse)
				.createdAt(resume.getCreatedAt()).updatedAt(resume.getUpdatedAt()).build();
	}

	public static AtsAnalysisResponse toAtsAnalysisResponse(AtsAnalysis ats) {
		return AtsAnalysisResponse.builder().id(ats.getId()).overallScore(ats.getOverallScore())
				.keywordMatchScore(ats.getKeywordMatchScore()).formatScore(ats.getFormatScore())
				.skillsScore(ats.getSkillsScore()).experienceScore(ats.getExperienceScore())
				.matchedKeywords(ats.getMatchedKeywords()).missingKeywords(ats.getMissingKeywords())
				.jobDescription(ats.getJobDescription()).jobTitle(ats.getJobTitle()).companyName(ats.getCompanyName())
				.improvements(ats.getImprovements()).suggestions(ats.getSuggestions())
				.totalKeywords(ats.getTotalKeywords()).matchedCount(ats.getMatchedCount()).createdAt(ats.getCreatedAt())
				.updatedAt(ats.getUpdatedAt()).build();
	}

	public static CoverLetterResponse toCoverLetterResponse(CoverLetter letter) {
		return CoverLetterResponse.builder().id(letter.getId()).content(letter.getContent())
				.jobDescription(letter.getJobDescription()).companyName(letter.getCompanyName())
				.jobTitle(letter.getJobTitle()).status(letter.getStatus()).createdAt(letter.getCreatedAt())
				.updatedAt(letter.getUpdatedAt()).build();
	}

	public static IndustryInsightResponse toIndustryInsightResponse(IndustryInsight insight) {
		return IndustryInsightResponse.builder().id(insight.getId()).industry(insight.getIndustry())
				.salaryRanges(insight.getSalaryRanges()).growthRate(insight.getGrowthRate())
				.demandLevel(insight.getDemandLevel()).topSkills(insight.getTopSkills())
				.marketOutlook(insight.getMarketOutlook()).keyTrends(insight.getKeyTrends())
				.recommendedSkills(insight.getRecommendedSkills()).lastUpdated(insight.getLastUpdated())
				.nextUpdate(insight.getNextUpdate()).build();
	}
}
