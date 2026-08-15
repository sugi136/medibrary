package com.medibrary.api.repository;

import com.medibrary.api.entity.SideEffectCache;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SideEffectCacheRepository extends JpaRepository<SideEffectCache, Long> {
    Optional<SideEffectCache> findFirstByDrugIdAndSourceOrderByFetchedAtDesc(String drugId, String source);
}
