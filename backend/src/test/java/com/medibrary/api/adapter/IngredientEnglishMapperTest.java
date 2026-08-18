package com.medibrary.api.adapter;

import com.medibrary.api.entity.Drug;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IngredientEnglishMapperTest {
    private final IngredientEnglishMapper mapper = new IngredientEnglishMapper();

    @Test
    void resolvesAcetaminophenFromProductNameIngredient() {
        Drug drug = new Drug("202106092", "타이레놀정500밀리그람(아세트아미노펜)");

        assertThat(mapper.resolve(drug)).contains("ACETAMINOPHEN");
    }

    @Test
    void preservesExistingEnglishIngredient() {
        Drug drug = new Drug("sample", "임의의 제품명");
        drug.setIngredientEn("IBUPROFEN");

        assertThat(mapper.resolve(drug)).contains("IBUPROFEN");
    }

    @Test
    void prefersSpecificIngredientOverContainedIngredient() {
        assertThat(mapper.resolveFromText("에스오메프라졸정"))
                .contains("ESOMEPRAZOLE");
    }

    @Test
    void resolvesEcabepideForNoResultOpenFdaHandling() {
        assertThat(mapper.resolveFromText("가베트정500밀리그램(에카베트나트륨수화물)"))
                .contains("ECABEPIDE");
    }
}
