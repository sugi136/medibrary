package com.medibrary.api.service.search;

import com.medibrary.api.adapter.GranuleClient;
import com.medibrary.api.entity.Drug;
import com.medibrary.api.repository.DrugRepository;
import com.medibrary.api.service.cache.DrugCacheService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class DrugSearchService {
    public record SearchPage(List<Drug> items, long totalCount, boolean hasNext) { }

    private record SearchMeta(long totalCount, Instant expiresAt) {
        boolean isFresh() {
            return Instant.now().isBefore(expiresAt);
        }
    }

    private static final Duration SEARCH_META_TTL = Duration.ofHours(24);

    private final DrugRepository drugRepository;
    private final GranuleClient granuleClient;
    private final DrugCacheService drugCacheService;
    private final ConcurrentMap<String, SearchMeta> searchMetaByCriteria = new ConcurrentHashMap<>();

    public DrugSearchService(DrugRepository drugRepository,
                             GranuleClient granuleClient,
                             DrugCacheService drugCacheService) {
        this.drugRepository = drugRepository;
        this.granuleClient = granuleClient;
        this.drugCacheService = drugCacheService;
    }

    @Transactional
    public SearchPage search(DrugSearchCriteria criteria, Pageable pageable) {
        if (criteria.isEmpty()) {
            return new SearchPage(List.of(), 0, false);
        }

        Page<Drug> cachedPage = findCached(criteria, pageable);
        SearchMeta cachedMeta = findFreshMeta(criteria);
        if (cachedPage.hasContent() && cachedMeta != null) {
            return new SearchPage(
                    cachedPage.getContent(),
                    cachedMeta.totalCount(),
                    hasNext(cachedMeta.totalCount(), pageable)
            );
        }
        if (isCompleteFirstCachedPage(cachedPage, pageable)) {
            return toSearchPage(cachedPage);
        }

        GranuleClient.SearchResult externalResult = granuleClient.search(
                criteria.normalizedName(),
                criteria.normalizedShape(),
                criteria.normalizedColor(),
                pageable.getPageNumber() + 1,
                pageable.getPageSize()
        );
        if (!externalResult.available()) {
            return toSearchPage(cachedPage);
        }

        List<Drug> items = filterExternalResults(
                drugCacheService.cacheAll(externalResult.items()),
                criteria
        );
        long totalCount = Math.max(externalResult.totalCount(), items.size());
        searchMetaByCriteria.put(criteriaKey(criteria),
                new SearchMeta(totalCount, Instant.now().plus(SEARCH_META_TTL)));
        return new SearchPage(items, totalCount, hasNext(totalCount, pageable));
    }

    private SearchMeta findFreshMeta(DrugSearchCriteria criteria) {
        String key = criteriaKey(criteria);
        SearchMeta meta = searchMetaByCriteria.get(key);
        if (meta != null && !meta.isFresh()) {
            searchMetaByCriteria.remove(key, meta);
            return null;
        }
        return meta;
    }

    private String criteriaKey(DrugSearchCriteria criteria) {
        return String.join("|", criteria.normalizedName(), criteria.normalizedShape(), criteria.normalizedColor());
    }

    private boolean isCompleteFirstCachedPage(Page<Drug> cachedPage, Pageable pageable) {
        return pageable.getPageNumber() == 0
                && cachedPage.hasContent()
                && cachedPage.getNumberOfElements() < pageable.getPageSize();
    }

    private SearchPage toSearchPage(Page<Drug> page) {
        return new SearchPage(page.getContent(), page.getTotalElements(), page.hasNext());
    }

    private boolean hasNext(long totalCount, Pageable pageable) {
        return totalCount > (long) (pageable.getPageNumber() + 1) * pageable.getPageSize();
    }

    private List<Drug> filterExternalResults(List<Drug> drugs, DrugSearchCriteria criteria) {
        if (!criteria.hasShapeAndColor()) return drugs;
        return drugs.stream()
                .filter(drug -> criteria.normalizedShape().equalsIgnoreCase(drug.getShape()))
                .filter(drug -> criteria.normalizedColor().equalsIgnoreCase(drug.getColor()))
                .toList();
    }

    private Page<Drug> findCached(DrugSearchCriteria criteria, Pageable pageable) {
        if (criteria.hasName()) {
            return drugRepository.findByNameContainingIgnoreCase(criteria.normalizedName(), pageable);
        }
        if (criteria.hasShapeAndColor()) {
            return drugRepository.findByShapeAndColor(
                    criteria.normalizedShape(), criteria.normalizedColor(), pageable);
        }
        if (criteria.hasShape()) {
            return drugRepository.findByShape(criteria.normalizedShape(), pageable);
        }
        return drugRepository.findByColor(criteria.normalizedColor(), pageable);
    }
}
