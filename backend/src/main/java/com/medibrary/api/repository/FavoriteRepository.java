package com.medibrary.api.repository;

import com.medibrary.api.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    List<Favorite> findAllByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<Favorite> findByIdAndUserId(Long favoriteId, Long userId);
    Optional<Favorite> findByUserIdAndDrugId(Long userId, String drugId);
    boolean existsByUserIdAndDrugId(Long userId, String drugId);
}
