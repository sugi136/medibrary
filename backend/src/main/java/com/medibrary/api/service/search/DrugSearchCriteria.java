package com.medibrary.api.service.search;

public record DrugSearchCriteria(String name, String shape, String color) {
    public boolean hasName() {
        return hasText(name);
    }

    public boolean hasShapeAndColor() {
        return hasText(shape) && hasText(color);
    }

    public boolean hasShape() {
        return hasText(shape);
    }

    public boolean hasColor() {
        return hasText(color);
    }

    public boolean isEmpty() {
        return !hasName() && !hasShape() && !hasColor();
    }

    public String normalizedName() {
        return normalize(name);
    }

    public String normalizedShape() {
        return normalize(shape);
    }

    public String normalizedColor() {
        return normalize(color);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String normalize(String value) {
        return value == null ? null : value.trim();
    }
}
