package com.medibrary.api.adapter;

import java.util.List;

public record ExternalContraindicationResult(boolean available, List<Item> items, String message) {
    public record Item(String drugId, String name, String type, String reason) { }

    public static ExternalContraindicationResult unavailable(String message) {
        return new ExternalContraindicationResult(false, List.of(), message);
    }

    public static ExternalContraindicationResult success(List<Item> items) {
        return new ExternalContraindicationResult(true, items, null);
    }
}
