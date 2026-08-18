package com.medibrary.api.adapter;

import java.util.List;

public record ExternalDrugInformation(
        boolean available,
        String ingredientKr,
        String efficacy,
        String usageInfo,
        String caution,
        List<String> sideEffects,
        String message
) {
    public static ExternalDrugInformation success(String ingredientKr,
                                                  String efficacy,
                                                  String usageInfo,
                                                  String caution,
                                                  List<String> sideEffects) {
        return new ExternalDrugInformation(true, ingredientKr, efficacy, usageInfo, caution, List.copyOf(sideEffects), null);
    }

    public static ExternalDrugInformation unavailable(String message) {
        return new ExternalDrugInformation(false, "", "", "", "", List.of(), message);
    }
}
