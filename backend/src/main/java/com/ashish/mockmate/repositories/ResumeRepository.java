package com.ashish.mockmate.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ashish.mockmate.models.Resume;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, UUID> {
	Optional<Resume> findByUserId(UUID userId);

	boolean existsByUserId(UUID userId);
}
