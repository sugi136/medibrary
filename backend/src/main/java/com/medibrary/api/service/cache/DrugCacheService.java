package com.medibrary.api.service.cache;

import com.medibrary.api.adapter.ExternalDrug;
import com.medibrary.api.entity.Drug;
import com.medibrary.api.repository.DrugRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DrugCacheService {
    private final DrugRepository drugRepository;

    public DrugCacheService(DrugRepository drugRepository) {
        this.drugRepository = drugRepository;
    }

    @Transactional
    public List<Drug> cacheAll(List<ExternalDrug> externalDrugs) {
        return externalDrugs.stream().map(this::cache).toList();
    }

    @Transactional
    public Drug cache(ExternalDrug external) {
        return drugRepository.findById(external.id())
                .orElseGet(() -> drugRepository.save(toEntity(external)));
    }

    private Drug toEntity(ExternalDrug external) {
        Drug drug = new Drug(external.id(), external.name());
        drug.setShape(external.shape());
        drug.setColor(external.color());
        drug.setMarkFront(external.markFront());
        drug.setMarkBack(external.markBack());
        drug.setImageUrl(external.imageUrl());
        return drug;
    }
}
