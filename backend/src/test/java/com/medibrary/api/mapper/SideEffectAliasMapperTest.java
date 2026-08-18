package com.medibrary.api.mapper;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SideEffectAliasMapperTest {
    private final SideEffectAliasMapper mapper = new SideEffectAliasMapper();

    @Test
    void identifiesKnownOverseasReactionInDomesticKoreanText() {
        assertThat(mapper.isMentionedInDomesticInformation("NAUSEA", "드물게 구역 및 구토가 나타날 수 있습니다."))
                .isTrue();
    }

    @Test
    void returnsFalseForUnknownReactionOrEmptyDomesticText() {
        assertThat(mapper.isMentionedInDomesticInformation("UNKNOWN EVENT", "구역이 나타날 수 있습니다."))
                .isFalse();
        assertThat(mapper.isMentionedInDomesticInformation("NAUSEA", "")).isFalse();
    }
}
