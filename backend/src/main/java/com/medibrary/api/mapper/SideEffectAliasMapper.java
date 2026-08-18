package com.medibrary.api.mapper;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * FAERS 영문 반응 용어가 국내 허가사항에 같은 의미로 언급됐는지 보조적으로 판정한다.
 * 이는 번역의 정확성을 보장하는 기능이 아니라, 국내 문서 내 유사 반응 언급 여부를 표시하는 규칙이다.
 */
@Component
public class SideEffectAliasMapper {
    private static final Map<String, List<String>> KOREAN_REACTION_ALIASES = Map.ofEntries(
            Map.entry("NAUSEA", List.of("구역")),
            Map.entry("VOMITING", List.of("구토")),
            Map.entry("DIZZINESS", List.of("어지러움")),
            Map.entry("RASH", List.of("발진")),
            Map.entry("FATIGUE", List.of("피로")),
            Map.entry("PYREXIA", List.of("발열")),
            Map.entry("HEADACHE", List.of("두통")),
            Map.entry("DIARRHOEA", List.of("설사")),
            Map.entry("DIARRHEA", List.of("설사")),
            Map.entry("PRURITUS", List.of("가려움")),
            Map.entry("URTICARIA", List.of("두드러기")),
            Map.entry("ANAPHYLACTIC REACTION", List.of("아나필락시스"))
    );

    public boolean isMentionedInDomesticInformation(String overseasTerm, String domesticCorpus) {
        if (domesticCorpus == null || domesticCorpus.isBlank()) {
            return false;
        }
        return KOREAN_REACTION_ALIASES.getOrDefault(normalize(overseasTerm), List.of()).stream()
                .anyMatch(domesticCorpus::contains);
    }

    private String normalize(String term) {
        return term == null ? "" : term.trim().toUpperCase(Locale.ROOT);
    }
}
