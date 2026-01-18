package com.layerten.repository;

import com.layerten.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Author entity.
 * Provides basic CRUD operations and lookup by email.
 */
@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {
    
    /**
     * Find an author by email address.
     * 
     * @param email the email to search for
     * @return an Optional containing the author if found
     */
    Optional<Author> findByEmail(String email);
}
