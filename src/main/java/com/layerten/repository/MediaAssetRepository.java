package com.layerten.repository;

import com.layerten.entity.MediaAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for MediaAsset entity.
 * Provides basic CRUD operations for media assets.
 */
@Repository
public interface MediaAssetRepository extends JpaRepository<MediaAsset, Long> {
    // Basic CRUD operations are inherited from JpaRepository
    // Additional custom query methods can be added here if needed
}
