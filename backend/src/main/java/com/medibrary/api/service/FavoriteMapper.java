package com.medibrary.api.service;

import com.medibrary.api.dto.FavoriteDtos;
import com.medibrary.api.entity.Favorite;

final class FavoriteMapper {
    private FavoriteMapper() { }

    static FavoriteDtos.FavoriteDrugSummary toSummary(Favorite favorite) {
        var drug = favorite.getDrug();
        return new FavoriteDtos.FavoriteDrugSummary(
                favorite.getId(),
                drug.getId(),
                drug.getName(),
                drug.getManufacturer(),
                drug.getShape(),
                drug.getColor(),
                drug.getImageUrl()
        );
    }
}
