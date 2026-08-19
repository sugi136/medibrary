package com.medibrary.api.service;

import com.medibrary.api.dto.DrugDtos;
import com.medibrary.api.entity.Drug;
import com.medibrary.api.repository.DrugRepository;
import com.medibrary.api.repository.FavoriteRepository;
import com.medibrary.api.security.CurrentUserProvider;
import com.medibrary.api.service.search.DrugSearchCriteria;
import com.medibrary.api.service.search.DrugSearchService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class DrugService {
    private final DrugRepository drugRepository;
    private final FavoriteRepository favoriteRepository;
    private final DrugSearchService drugSearchService;
    private final DrugInformationEnrichmentService drugInformationEnrichmentService;
    private final SearchHistoryService searchHistoryService;
    private final CurrentUserProvider currentUserProvider;

    public DrugService(DrugRepository drugRepository,
                       FavoriteRepository favoriteRepository,
                       DrugSearchService drugSearchService,
                       DrugInformationEnrichmentService drugInformationEnrichmentService,
                       SearchHistoryService searchHistoryService,
                       CurrentUserProvider currentUserProvider) {
        this.drugRepository = drugRepository;
        this.favoriteRepository = favoriteRepository;
        this.drugSearchService = drugSearchService;
        this.drugInformationEnrichmentService = drugInformationEnrichmentService;
        this.searchHistoryService = searchHistoryService;
        this.currentUserProvider = currentUserProvider;
    }

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 50;

    @Transactional
    public DrugDtos.SearchResponse search(String name, String shape, String color,
                                          int requestedPage, int requestedSize) {
        int page = Math.max(0, requestedPage);
        int size = Math.min(MAX_PAGE_SIZE, Math.max(1, requestedSize > 0 ? requestedSize : DEFAULT_PAGE_SIZE));
        DrugSearchCriteria criteria = new DrugSearchCriteria(name, shape, color);
        var result = drugSearchService.search(
                criteria,
                PageRequest.of(page, size, Sort.by(Sort.Order.asc("name"), Sort.Order.asc("id")))
        );
        List<DrugDtos.DrugSummary> items = result.items().stream()
                .map(DrugMapper::toSummary)
                .toList();
        if (page == 0) {
            currentUserProvider.getUserId().ifPresent(userId -> searchHistoryService.record(userId, criteria));
        }
        return new DrugDtos.SearchResponse(result.totalCount(), page, size, result.hasNext(), items);
    }

    @Transactional
    public DrugDtos.DrugDetail getDetail(String drugId) {
        Drug drug = findDrug(drugId);
        drugInformationEnrichmentService.enrichMissingFields(drug);
        boolean isFavorite = currentUserProvider.getUserId()
                .map(userId -> favoriteRepository.existsByUserIdAndDrugId(userId, drugId))
                .orElse(false);
        return DrugMapper.toDetail(drug, isFavorite);
    }

    @Transactional(readOnly = true)
    public Drug findDrug(String drugId) {
        return drugRepository.findById(drugId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 약입니다."));
    }
}
