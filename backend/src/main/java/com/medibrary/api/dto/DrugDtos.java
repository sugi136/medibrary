package com.medibrary.api.dto;

import java.util.List;

public final class DrugDtos {
    private DrugDtos() { }

    public record DrugSummary(
            String id, String name, String manufacturer, String shape, String color, String imageUrl
    ) { }

    public record DrugDetail(
            String id, String name, String manufacturer, String shape, String color, String imageUrl,
            String markFront, String markBack, String ingredientEn, String efficacy, String usage,
            String caution, boolean isFavorite
    ) { }

    public record SearchResponse(long totalCount, List<DrugSummary> items) { }

    public record SideEffectResult(boolean available, List<String> cases, String message) { }

    public record SideEffectsResponse(SideEffectResult domestic, SideEffectResult overseas) { }

    public record ContraindicationItem(
            String drugId, String name, String manufacturer, String type, String reason
    ) { }

    public record ContraindicationResponse(
            String drugId, boolean available, List<ContraindicationItem> items, String message
    ) { }
}
