package com.ashish.mockmate.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ashish.mockmate.models.CoverLetter;

@Repository
public interface CoverLetterRepository extends JpaRepository<CoverLetter, String> {
	List<CoverLetter> findByUserIdOrderByCreatedAtDesc(UUID userId);

	Page<CoverLetter> findByUserId(UUID userId, Pageable pageable);

	Optional<CoverLetter> findByIdAndUserId(String id, UUID userId);

	void deleteByIdAndUserId(String id, UUID userId);
}
