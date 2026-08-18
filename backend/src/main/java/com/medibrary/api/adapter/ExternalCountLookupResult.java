package com.medibrary.api.adapter;

import java.util.List;

public record ExternalCountLookupResult(boolean available,
                                        List<ExternalSideEffectCount> items,
                                        String message) {
    public static ExternalCountLookupResult success(List<ExternalSideEffectCount> items) {
        return new ExternalCountLookupResult(true, List.copyOf(items), null);
    }

    public static ExternalCountLookupResult unavailable(String message) {
        return new ExternalCountLookupResult(false, List.of(), message);
    }
}
