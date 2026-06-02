package com.ashish.mockmate.services.impl;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ashish.mockmate.dto.response.IndustryInsightResponse;
import com.ashish.mockmate.exception.ResourceNotFoundException;
import com.ashish.mockmate.models.IndustryInsight;
import com.ashish.mockmate.repositories.IndustryInsightRepository;
import com.ashish.mockmate.repositories.UserRepository;
import com.ashish.mockmate.security.ClerkPrincipal;
import com.ashish.mockmate.services.GeminiService;
import com.ashish.mockmate.services.IndustryInsightService;
import com.ashish.mockmate.util.EntityMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class IndustryInsightServiceImpl implements IndustryInsightService {

	private final IndustryInsightRepository insightRepository;
	private final UserRepository userRepository;
	private final GeminiService geminiService;
	private final ObjectMapper objectMapper;

	@Override
	@Transactional
	public IndustryInsightResponse getInsightsForUser(ClerkPrincipal principal) {
		var user = userRepository.findByClerkUserId(principal.getClerkUserId())
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		if (user.getIndustry() == null) {
			throw new ResourceNotFoundException("User has not selected an industry. Please complete onboarding.");
		}

		// FIX BUG 6: Do NOT call getOrCreateInsight(industry) from here.
		// Same-class self-invocation bypasses Spring's @Transactional proxy.
		// Inline the logic directly so the existing transaction is used.
		return fetchOrCreate(user.getIndustry());
	}

	@Override
	@Transactional
	public IndustryInsightResponse getInsightsByIndustry(String industry) {
		// FIX BUG 6: Same fix - inline instead of calling annotated method on self.
		return fetchOrCreate(industry);
	}

	/**
	 * Private helper — called only within already-transactional public methods.
	 * No @Transactional needed here since the caller's transaction is already
	 * active.
	 */
	private IndustryInsightResponse fetchOrCreate(String industry) {
		Optional<IndustryInsight> existing = insightRepository.findByIndustry(industry);

		if (existing.isPresent()) {
			IndustryInsight insight = existing.get();
			if (insight.getNextUpdate().isBefore(LocalDateTime.now())) {
				log.info("Refreshing stale industry insight for: {}", industry);
				return refreshInsight(insight);
			}
			return EntityMapper.toIndustryInsightResponse(insight);
		}

		log.info("Creating new industry insight for: {}", industry);
		return createInsight(industry);
	}

	private IndustryInsightResponse createInsight(String industry) {
		String aiResponse = geminiService.generateIndustryInsights(industry);
		IndustryInsight insight = parseAndBuildInsight(industry, aiResponse, null);
		IndustryInsight saved = insightRepository.save(insight);
		return EntityMapper.toIndustryInsightResponse(saved);
	}

	private IndustryInsightResponse refreshInsight(IndustryInsight existing) {
		String aiResponse = geminiService.generateIndustryInsights(existing.getIndustry());
		IndustryInsight updated = parseAndBuildInsight(existing.getIndustry(), aiResponse, existing);
		IndustryInsight saved = insightRepository.save(updated);
		return EntityMapper.toIndustryInsightResponse(saved);
	}

	@SuppressWarnings("unchecked")
	private IndustryInsight parseAndBuildInsight(String industry, String aiResponse, IndustryInsight existing) {
		try {
			String cleaned = aiResponse.trim().replaceAll("```json\\s*", "").replaceAll("```\\s*", "");

			Map<String, Object> data = objectMapper.readValue(cleaned, new TypeReference<>() {
			});

			String[] topSkills = toStringArray(data, "topSkills");
			String[] keyTrends = toStringArray(data, "keyTrends");
			String[] recSkills = toStringArray(data, "recommendedSkills");
			List<Map<String, Object>> salaryRanges = getListOfMaps(data, "salaryRanges");
			double growthRate = getDouble(data, "growthRate");
			String demandLevel = getString(data, "demandLevel", "Medium");
			String outlook = getString(data, "marketOutlook", "neutral");

			if (existing != null) {
				existing.setGrowthRate(growthRate);
				existing.setDemandLevel(demandLevel);
				existing.setMarketOutlook(outlook);
				existing.setTopSkills(topSkills);
				existing.setKeyTrends(keyTrends);
				existing.setRecommendedSkills(recSkills);
				existing.setSalaryRanges(salaryRanges);
				existing.setLastUpdated(LocalDateTime.now());
				existing.setNextUpdate(LocalDateTime.now().plusWeeks(1));
				return existing;
			}

			return IndustryInsight.builder().industry(industry).growthRate(growthRate).demandLevel(demandLevel)
					.marketOutlook(outlook).topSkills(topSkills).keyTrends(keyTrends).recommendedSkills(recSkills)
					.salaryRanges(salaryRanges).lastUpdated(LocalDateTime.now())
					.nextUpdate(LocalDateTime.now().plusWeeks(1)).build();

		} catch (Exception e) {
			log.error("Failed to parse industry insights for {}: {}", industry, e.getMessage());
			if (existing != null) {
				existing.setLastUpdated(LocalDateTime.now());
				existing.setNextUpdate(LocalDateTime.now().plusDays(1));
				return existing;
			}
			return IndustryInsight.builder().industry(industry).growthRate(5.0).demandLevel("Medium")
					.marketOutlook("neutral").lastUpdated(LocalDateTime.now())
					.nextUpdate(LocalDateTime.now().plusDays(1)).build();
		}
	}

	@Scheduled(cron = "${mockmate.scheduler.industry-insights-cron}")
	@Transactional
	public void refreshStaleInsights() {
		List<IndustryInsight> stale = insightRepository.findAllDueForUpdate(LocalDateTime.now());
		log.info("Refreshing {} stale industry insights", stale.size());
		stale.forEach(insight -> {
			try {
				refreshInsight(insight);
			} catch (Exception e) {
				log.error("Failed to refresh insight for {}: {}", insight.getIndustry(), e.getMessage());
			}
		});
	}

	// ---- helpers ----

	private String[] toStringArray(Map<String, Object> map, String key) {
		Object val = map.get(key);
		if (val instanceof List<?> list)
			return list.stream().map(Object::toString).toArray(String[]::new);
		return new String[] {};
	}

	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> getListOfMaps(Map<String, Object> map, String key) {
		Object val = map.get(key);
		if (val instanceof List)
			return (List<Map<String, Object>>) val;
		return List.of();
	}

	private double getDouble(Map<String, Object> map, String key) {
		Object val = map.get(key);
		if (val instanceof Number n)
			return n.doubleValue();
		return 0.0;
	}

	private String getString(Map<String, Object> map, String key, String def) {
		Object val = map.get(key);
		return val != null ? val.toString() : def;
	}
}