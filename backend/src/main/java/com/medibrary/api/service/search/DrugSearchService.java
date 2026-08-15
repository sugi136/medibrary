package com.medibrary.api.service.search;

import com.medibrary.api.adapter.GranuleClient;
import com.medibrary.api.entity.Drug;
import com.medibrary.api.repository.DrugRepository;
import com.medibrary.api.service.cache.DrugCacheService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DrugSearchService {
    private final DrugRepository drugRepository;
    private final GranuleClient granuleClient;
    private final DrugCacheService drugCacheService;

    public DrugSearchService(DrugRepository drugRepository, GranuleClient granuleClient, DrugCacheService drugCacheService) {
        this.drugRepository = drugRepository;
        this.granuleClient = granuleClient;
        this.drugCacheService = drugCacheService;
    }

    @Transactional
    public List<Drug> search(DrugSearchCriteria criteria) {
        List<Drug> cachedDrugs = findCached(criteria);
        if (!cachedDrugs.isEmpty() || criteria.isEmpty()) {
            return cachedDrugs;
        }
        return drugCacheService.cacheAll(granuleClient.search(
                criteria.normalizedName(), criteria.normalizedShape(), criteria.normalizedColor()));
    }

    private List<Drug> findCached(DrugSearchCriteria criteria) {
        if (criteria.hasName()) {
            return drugRepository.findTop20ByNameContainingIgnoreCase(criteria.normalizedName());
        }
        if (criteria.hasShapeAndColor()) {
            return drugRepository.findTop20ByShapeAndColor(criteria.normalizedShape(), criteria.normalizedColor());
        }
        if (criteria.hasShape()) {
            return drugRepository.findTop20ByShape(criteria.normalizedShape());
        }
        if (criteria.hasColor()) {
            return drugRepository.findTop20ByColor(criteria.normalizedColor());
        }
        return List.of();
    }
}
