package com.medibrary.api.dto;

import java.util.List;

public final class DrugDtos {
    private DrugDtos() { }

    public record DrugSummary(
            String id, String name, String manufacturer, String shape, String color, String imageUrl
    ) { }

    public record DrugDetail(
            String id, String name, String manufacturer, String shape, String color, String imageUrl,
            String markFront, String markBack, String ingredientKr, String ingredientEn, String efficacy,
            String usageInfo, String caution, boolean isFavorite
    ) { }

    public record SearchResponse(long totalCount, List<DrugSummary> items) { }

    public record SideEffectCase(String term, Long count, boolean domesticMentioned) { }

    public record SideEffectResult(boolean available, List<SideEffectCase> cases, String message) { }

    public record SideEffectsResponse(SideEffectResult domestic,
                                      SideEffectResult overseas,
                                      String disclaimer) { }

    public record ContraindicationItem(
            String drugId, String name, String manufacturer, String type, String reason
    ) { }

    public record ContraindicationResponse(
            String drugId, boolean available, List<ContraindicationItem> items, String message
    ) { }

    public record DuplicateWarningItem(String drugId, String name, String category, String reason) { }

    public record DuplicateWarningResponse(String drugId,
                                           boolean available,
                                           List<DuplicateWarningItem> sameIngredientItems,
                                           List<DuplicateWarningItem> efficacyGroupItems,
                                           String message) { }
}
