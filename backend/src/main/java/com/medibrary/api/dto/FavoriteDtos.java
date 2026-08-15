package com.medibrary.api.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public final class FavoriteDtos {
    private FavoriteDtos() { }

    public record CreateFavoriteRequest(@NotBlank String drugId) { }

    public record FavoriteDrugSummary(
            Long favoriteId,
            String id,
            String name,
            String manufacturer,
            String shape,
            String color,
            String imageUrl
    ) { }

    public record DurWarning(String drugIdA, String drugIdB, String reason, String severity) { }

    public record FavoritesResponse(List<FavoriteDrugSummary> favorites, List<DurWarning> durWarnings) { }

    public record DashboardSummary(List<String> recentSearches, long favoriteCount, boolean hasDurWarning) { }
}
