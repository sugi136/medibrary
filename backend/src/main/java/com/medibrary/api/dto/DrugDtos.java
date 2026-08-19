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

    public record SearchResponse(long totalCount, int page, int size, boolean hasNext, List<DrugSummary> items) { }

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

    public record OverseasProduct(String name,
                                  String genericName,
                                  String labeler,
                                  String dosageForm,
                                  String route,
                                  String activeIngredients,
                                  String productNdc,
                                  String country) { }

    public record OverseasProductResponse(String drugId,
                                          String ingredientEn,
                                          boolean available,
                                          List<OverseasProduct> items,
                                          String message,
                                          String disclaimer) {
        private static final String DISCLAIMER =
                "동일 주성분 기준으로 찾은 미국 유통 제품입니다. 함량·제형·허가사항이 국내 제품과 다를 수 있으며, "
                        + "해외 의약품의 국내 반입 및 복용은 반드시 의사·약사와 상담하세요.";

        public static OverseasProductResponse success(String drugId, String ingredientEn,
                                                       List<OverseasProduct> items) {
            return new OverseasProductResponse(drugId, ingredientEn, true, items, null, DISCLAIMER);
        }

        public static OverseasProductResponse empty(String drugId, String ingredientEn, String message) {
            return new OverseasProductResponse(drugId, ingredientEn, true, List.of(), message, DISCLAIMER);
        }

        public static OverseasProductResponse unavailable(String drugId, String ingredientEn, String message) {
            return new OverseasProductResponse(drugId, ingredientEn, false, List.of(), message, DISCLAIMER);
        }
    }
}
