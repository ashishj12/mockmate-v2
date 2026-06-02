package com.ashish.mockmate.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ashish.mockmate.models.AtsAnalysis;

@Repository
public interface AtsAnalysisRepository extends JpaRepository<AtsAnalysis, String> {
	Optional<AtsAnalysis> findByResumeId(UUID resumeId);

	@Modifying
	@Query("DELETE FROM AtsAnalysis a WHERE a.resume.id = :resumeId")
	void deleteByResumeId(@Param("resumeId") UUID resumeId);
}
