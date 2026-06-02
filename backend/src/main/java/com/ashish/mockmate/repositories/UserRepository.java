package com.ashish.mockmate.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ashish.mockmate.models.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
	Optional<User> findByClerkUserId(String clerkUserId);

	Optional<User> findByEmail(String email);

	boolean existsByClerkUserId(String clerkUserId);

	boolean existsByEmail(String email);
}
