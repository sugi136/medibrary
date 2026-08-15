package com.medibrary.api.service;

import com.medibrary.api.service.search.DrugSearchCriteria;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DrugSearchCriteriaTest {
    @Test
    void nameSearch_shouldTrimKeywordAndPreferNameCondition() {
        DrugSearchCriteria criteria = new DrugSearchCriteria("  타이레놀  ", "원형", "흰색");

        assertThat(criteria.hasName()).isTrue();
        assertThat(criteria.normalizedName()).isEqualTo("타이레놀");
        assertThat(criteria.isEmpty()).isFalse();
    }

    @Test
    void emptyCriteria_shouldNotRequestExternalSearch() {
        DrugSearchCriteria criteria = new DrugSearchCriteria(" ", null, "");

        assertThat(criteria.isEmpty()).isTrue();
        assertThat(criteria.hasShapeAndColor()).isFalse();
    }
}
