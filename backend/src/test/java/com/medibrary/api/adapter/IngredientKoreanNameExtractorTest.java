package com.medibrary.api.adapter;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IngredientKoreanNameExtractorTest {
    @Test
    void extractsKoreanIngredientFromProductNameParentheses() {
        assertThat(IngredientKoreanNameExtractor.extract("타이레놀정500밀리그람(아세트아미노펜)"))
                .contains("아세트아미노펜");
    }

    @Test
    void returnsEmptyWhenProductNameHasNoKoreanParenthesizedIngredient() {
        assertThat(IngredientKoreanNameExtractor.extract("TEST TABLET (ACETAMINOPHEN)"))
                .isEmpty();
    }
}
