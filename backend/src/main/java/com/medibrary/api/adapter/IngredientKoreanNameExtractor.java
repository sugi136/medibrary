package com.medibrary.api.adapter;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 공공 의약품 제품명에 포함된 괄호 표기에서 한글 주성분 후보를 추출한다.
 */
public final class IngredientKoreanNameExtractor {
    private static final Pattern PARENTHESIZED_TEXT = Pattern.compile("\\(([^()]*)\\)");
    private static final Pattern HANGUL = Pattern.compile("[가-힣]");

    private IngredientKoreanNameExtractor() { }

    public static Optional<String> extract(String productName) {
        if (productName == null || productName.isBlank()) {
            return Optional.empty();
        }
        Matcher matcher = PARENTHESIZED_TEXT.matcher(productName);
        while (matcher.find()) {
            String candidate = matcher.group(1).replaceAll("\\s+", " ").trim();
            if (!candidate.isBlank() && HANGUL.matcher(candidate).find()) {
                return Optional.of(candidate);
            }
        }
        return Optional.empty();
    }
}
