package com.medibrary.api.service.cache;

import com.medibrary.api.adapter.ExternalDrug;
import com.medibrary.api.adapter.IngredientKoreanNameExtractor;
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
                .map(existing -> applyExternalFields(existing, external))
                .orElseGet(() -> drugRepository.save(toEntity(external)));
    }

    private Drug toEntity(ExternalDrug external) {
        Drug drug = new Drug(external.id(), external.name());
        return applyExternalFields(drug, external);
    }

    private Drug applyExternalFields(Drug drug, ExternalDrug external) {
        drug.setManufacturer(preferNonBlank(external.manufacturer(), drug.getManufacturer()));
        drug.setIngredientKr(preferNonBlank(
                IngredientKoreanNameExtractor.extract(external.name()).orElse(null), drug.getIngredientKr()));
        drug.setShape(preferNonBlank(external.shape(), drug.getShape()));
        drug.setColor(preferNonBlank(external.color(), drug.getColor()));
        drug.setMarkFront(preferNonBlank(external.markFront(), drug.getMarkFront()));
        drug.setMarkBack(preferNonBlank(external.markBack(), drug.getMarkBack()));
        drug.setImageUrl(preferNonBlank(external.imageUrl(), drug.getImageUrl()));
        return drug;
    }

    private String preferNonBlank(String preferred, String fallback) {
        return preferred == null || preferred.isBlank() ? fallback : preferred;
    }
}
