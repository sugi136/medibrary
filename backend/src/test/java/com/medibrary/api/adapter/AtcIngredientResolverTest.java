package com.medibrary.api.adapter;

import com.medibrary.api.entity.Drug;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AtcIngredientResolverTest {
    private AtcIngredientResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new AtcIngredientResolver(new IngredientEnglishMapper());
        resolver.loadResources();
    }

    @Test
    void resolvesAtcIngredientAndUsAliasTerms() {
        Drug drug = new Drug("202200407", "타이레놀8시간이알서방정(아세트아미노펜)");
        drug.setAtcCode("N02BE01");

        assertThat(resolver.resolvePrimary(drug)).contains("paracetamol");
        assertThat(resolver.resolveSearchTerms(drug)).containsExactly("paracetamol", "acetaminophen");
    }

    @Test
    void usesExistingEnglishIngredientBeforeAtcLookup() {
        Drug drug = new Drug("sample", "테스트정");
        drug.setIngredientEn("ACETAMINOPHEN");
        drug.setAtcCode("N02BE01");

        assertThat(resolver.resolvePrimary(drug)).contains("ACETAMINOPHEN");
    }
}
