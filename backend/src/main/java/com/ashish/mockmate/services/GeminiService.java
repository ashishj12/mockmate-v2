package com.ashish.mockmate.services;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService {

	private final WebClient geminiWebClient;
	private final ObjectMapper objectMapper;

	@Value("${gemini.api-key}")
	private String apiKey;

	@Value("${gemini.model}")
	private String model;

	@Value("${gemini.max-tokens}")
	private int maxTokens;

	@Value("${gemini.temperature}")
	private double temperature;

	/**
	 * Send a prompt to Gemini and get back a text response.
	 */
	public String generateContent(String prompt) {
		Map<String, Object> requestBody = Map.of("contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
				"generationConfig", Map.of("temperature", temperature, "maxOutputTokens", maxTokens));

		try {
			String response = geminiWebClient.post().uri("/{model}:generateContent?key={apiKey}", model, apiKey)
					.bodyValue(requestBody).retrieve().bodyToMono(String.class).block();

			JsonNode root = objectMapper.readTree(response);
			return root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
		} catch (Exception e) {
			log.error("Gemini API call failed: {}", e.getMessage());
			throw new RuntimeException("AI service unavailable: " + e.getMessage());
		}
	}

	// ============================================================
	// RESUME GENERATION
	// ============================================================
	public String generateResume(String userBio, String experience, String skills, String industry) {
		String prompt = String.format("""
				You are an expert resume writer. Create a professional, ATS-optimized resume in Markdown format.

				User Details:
				- Industry: %s
				- Years of Experience: %s
				- Skills: %s
				- Bio/Summary: %s

				Requirements:
				1. Use clean Markdown formatting with proper headers (##, ###)
				2. Include sections: Summary, Skills, Experience, Education, Projects
				3. Use bullet points for responsibilities and achievements
				4. Use action verbs and quantifiable achievements where possible
				5. Keep it ATS-friendly - no tables, no columns, no graphics
				6. Tailor content for the %s industry

				Return ONLY the Markdown resume content, nothing else.
				""", industry, experience, skills, userBio, industry);

		return generateContent(prompt);
	}

	// ============================================================
	// ATS ANALYSIS
	// ============================================================
	public String analyzeResumeForAts(String resumeContent, String jobDescription, String jobTitle,
			String companyName) {
		String prompt = String.format(
				"""
						You are an expert ATS (Applicant Tracking System) analyzer.

						Analyze this resume against the job description and return a JSON object with the following structure:
						{
						  "overallScore": <0-100>,
						  "keywordMatchScore": <0-100>,
						  "formatScore": <0-100>,
						  "skillsScore": <0-100>,
						  "experienceScore": <0-100>,
						  "matchedKeywords": ["keyword1", "keyword2", ...],
						  "missingKeywords": ["keyword1", "keyword2", ...],
						  "totalKeywords": <number>,
						  "matchedCount": <number>,
						  "improvements": [
						    {"type": "keyword|format|experience|skill", "title": "...", "description": "...", "priority": "high|medium|low"}
						  ],
						  "suggestions": [
						    {"category": "...", "suggestion": "...", "impact": "high|medium|low"}
						  ]
						}

						Resume:
						%s

						Job Title: %s
						Company: %s
						Job Description:
						%s

						Return ONLY valid JSON, no markdown, no explanation.
						""",
				resumeContent, jobTitle != null ? jobTitle : "Not specified",
				companyName != null ? companyName : "Not specified", jobDescription);

		return generateContent(prompt);
	}

	// ============================================================
	// COVER LETTER GENERATION
	// ============================================================
	public String generateCoverLetter(String userBio, String skills, String experience, String companyName,
			String jobTitle, String jobDescription) {
		String prompt = String.format(
				"""
						You are a professional cover letter writer. Write a compelling, personalized cover letter in Markdown format.

						Candidate Information:
						- Bio/Summary: %s
						- Skills: %s
						- Years of Experience: %s

						Job Details:
						- Company: %s
						- Position: %s
						- Job Description: %s

						Requirements:
						1. Write in a professional yet personable tone
						2. Reference specific skills that match the job description
						3. Show genuine enthusiasm for the company/role
						4. Keep it to 3-4 paragraphs
						5. Include a strong opening and closing
						6. Use Markdown formatting
						7. Do NOT include placeholder text like [Your Name] - write as if from a candidate

						Return ONLY the Markdown cover letter content.
						""",
				userBio, skills, experience, companyName, jobTitle,
				jobDescription != null ? jobDescription : "Not provided");

		return generateContent(prompt);
	}

	// ============================================================
	// INTERVIEW QUESTIONS GENERATION
	// ============================================================
	public String generateInterviewQuestions(String category, String industry, String skills, int count) {
		String prompt = String.format("""
				You are an expert technical interviewer. Generate %d %s interview questions for a %s professional.

				Relevant Skills: %s

				Return a JSON array with this structure:
				[
				  {
				    "question": "...",
				    "answer": "...",
				    "difficulty": "easy|medium|hard",
				    "topic": "..."
				  }
				]

				For "technical" category: focus on coding, system design, algorithms, and %s-specific concepts.
				For "behavioral" category: use STAR format questions about teamwork, conflict, leadership.

				Return ONLY valid JSON array, no markdown, no explanation.
				""", count, category, industry, skills, industry);

		return generateContent(prompt);
	}

	// ============================================================
	// IMPROVEMENT TIP GENERATION
	// ============================================================
	public String generateImprovementTip(String category, List<Map<String, Object>> incorrectQuestions, Double score) {
		String prompt = String.format("""
				You are a career coach. A candidate scored %.1f%% on a %s interview assessment.

				They got these questions wrong:
				%s

				Provide a concise, actionable improvement tip (2-3 sentences) focusing on:
				1. The main knowledge gap identified
				2. A specific learning recommendation
				3. Encouragement

				Return ONLY the tip text, no JSON, no markdown headers.
				""", score, category, incorrectQuestions.toString());

		return generateContent(prompt);
	}

	// ============================================================
	// INDUSTRY INSIGHTS GENERATION
	// ============================================================
	public String generateIndustryInsights(String industry) {
		String prompt = String.format("""
				You are a market research analyst. Generate comprehensive industry insights for the %s industry.

				Return a JSON object with this exact structure:
				{
				  "growthRate": <percentage as decimal e.g. 8.5>,
				  "demandLevel": "High|Medium|Low",
				  "marketOutlook": "positive|negative|neutral",
				  "topSkills": ["skill1", "skill2", "skill3", "skill4", "skill5"],
				  "keyTrends": ["trend1", "trend2", "trend3", "trend4"],
				  "recommendedSkills": ["skill1", "skill2", "skill3", "skill4", "skill5"],
				  "salaryRanges": [
				    {"role": "Junior", "min": 50000, "max": 80000, "currency": "USD"},
				    {"role": "Mid-level", "min": 80000, "max": 120000, "currency": "USD"},
				    {"role": "Senior", "min": 120000, "max": 180000, "currency": "USD"},
				    {"role": "Lead/Principal", "min": 160000, "max": 220000, "currency": "USD"}
				  ]
				}

				Base this on current market conditions (2024-2025). Return ONLY valid JSON, no explanation.
				""", industry);

		return generateContent(prompt);
	}
}
