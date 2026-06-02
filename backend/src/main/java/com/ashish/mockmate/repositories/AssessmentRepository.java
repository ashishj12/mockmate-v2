package com.ashish.mockmate.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ashish.mockmate.models.Assessment;

@Repository
public interface AssessmentRepository extends JpaRepository<Assessment, UUID> {
	List<Assessment> findByUserIdOrderByCreatedAtDesc(UUID userId);

	Page<Assessment> findByUserId(UUID userId, Pageable pageable);

	List<Assessment> findByUserIdAndCategoryOrderByCreatedAtDesc(UUID userId, String category);

	long countByUserId(UUID userId);

	// FIX BUG 4: Ownership-safe single-query fetch — no lazy proxy traversal
	// needed.
	Optional<Assessment> findByIdAndUserId(UUID id, UUID userId);
}
