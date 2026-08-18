package com.medibrary.api.service;

import com.medibrary.api.dto.DrugDtos;
import com.medibrary.api.entity.Drug;

final class DrugMapper {
    private DrugMapper() { }

    static DrugDtos.DrugSummary toSummary(Drug drug) {
        return new DrugDtos.DrugSummary(
                drug.getId(), drug.getName(), drug.getManufacturer(), drug.getShape(), drug.getColor(), drug.getImageUrl()
        );
    }

    static DrugDtos.DrugDetail toDetail(Drug drug, boolean isFavorite) {
        return new DrugDtos.DrugDetail(
                drug.getId(), drug.getName(), drug.getManufacturer(), drug.getShape(), drug.getColor(), drug.getImageUrl(),
                drug.getMarkFront(), drug.getMarkBack(), drug.getIngredientKr(), drug.getIngredientEn(), drug.getEfficacy(),
                drug.getUsageInfo(), drug.getCaution(), isFavorite
        );
    }
}
