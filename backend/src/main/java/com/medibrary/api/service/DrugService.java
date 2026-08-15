package com.medibrary.api.service;

import com.medibrary.api.dto.DrugDtos;
import com.medibrary.api.entity.Drug;
import com.medibrary.api.repository.DrugRepository;
import com.medibrary.api.repository.FavoriteRepository;
import com.medibrary.api.security.CurrentUserProvider;
import com.medibrary.api.service.search.DrugSearchCriteria;
import com.medibrary.api.service.search.DrugSearchService;
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
    private final CurrentUserProvider currentUserProvider;

    public DrugService(DrugRepository drugRepository,
                       FavoriteRepository favoriteRepository,
                       DrugSearchService drugSearchService,
                       DrugInformationEnrichmentService drugInformationEnrichmentService,
                       CurrentUserProvider currentUserProvider) {
        this.drugRepository = drugRepository;
        this.favoriteRepository = favoriteRepository;
        this.drugSearchService = drugSearchService;
        this.drugInformationEnrichmentService = drugInformationEnrichmentService;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional
    public DrugDtos.SearchResponse search(String name, String shape, String color) {
        List<DrugDtos.DrugSummary> items = drugSearchService
                .search(new DrugSearchCriteria(name, shape, color))
                .stream()
                .map(DrugMapper::toSummary)
                .toList();
        return new DrugDtos.SearchResponse(items.size(), items);
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
