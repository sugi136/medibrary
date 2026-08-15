package com.medibrary.api.service.cache;

import com.medibrary.api.entity.Drug;
import com.medibrary.api.entity.SideEffectCache;
import com.medibrary.api.repository.SideEffectCacheRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class SideEffectCacheService {
    private static final Duration TTL = Duration.ofHours(24);

    private final SideEffectCacheRepository cacheRepository;
    private final EntityManager entityManager;

    public SideEffectCacheService(SideEffectCacheRepository cacheRepository, EntityManager entityManager) {
        this.cacheRepository = cacheRepository;
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public Optional<List<String>> findFresh(String drugId, String source) {
        return cacheRepository.findFirstByDrugIdAndSourceOrderByFetchedAtDesc(drugId, source)
                .filter(cache -> cache.getFetchedAt() != null)
                .filter(cache -> Duration.between(cache.getFetchedAt(), LocalDateTime.now()).compareTo(TTL) < 0)
                .map(cache -> deserialize(cache.getContent()));
    }

    @Transactional
    public void save(String drugId, String source, List<String> items) {
        Drug drugReference = entityManager.getReference(Drug.class, drugId);
        cacheRepository.save(new SideEffectCache(drugReference, source, String.join("\n", items)));
    }

    private List<String> deserialize(String content) {
        return content == null || content.isBlank() ? List.of() : Arrays.asList(content.split("\\R"));
    }
}
