package com.medibrary.api.adapter;

import java.util.List;

public record ExternalLookupResult(boolean available, List<String> items, String message) {
    public static ExternalLookupResult success(List<String> items) {
        return new ExternalLookupResult(true, items, null);
    }

    public static ExternalLookupResult unavailable(String message) {
        return new ExternalLookupResult(false, List.of(), message);
    }
}
