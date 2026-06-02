package com.ashish.mockmate.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ashish.mockmate.models.IndustryInsight;

@Repository
public interface IndustryInsightRepository extends JpaRepository<IndustryInsight, String> {
	Optional<IndustryInsight> findByIndustry(String industry);

	boolean existsByIndustry(String industry);

	@Query("SELECT i FROM IndustryInsight i WHERE i.nextUpdate <= :now")
	List<IndustryInsight> findAllDueForUpdate(LocalDateTime now);
}
