package com.medibrary.api.service;

import com.medibrary.api.adapter.ExternalCountLookupResult;
import com.medibrary.api.adapter.ExternalLookupResult;
import com.medibrary.api.adapter.ExternalSideEffectCount;
import com.medibrary.api.adapter.EyakClient;
import com.medibrary.api.adapter.OpenFdaClient;
import com.medibrary.api.dto.DrugDtos;
import com.medibrary.api.entity.Drug;
import com.medibrary.api.service.cache.SideEffectCacheService;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

@Service
public class SideEffectService {
    private static final String DOMESTIC = "domestic";
    private static final String OVERSEAS = "overseas";
    private static final String FAERS_DISCLAIMER =
            "FAERS 자발보고 건수는 발생 빈도나 인과관계를 뜻하지 않습니다. 처방량으로 보정되지 않았으며, "
                    + "한 보고에 여러 약물·반응이 함께 포함될 수 있습니다.";
    private static final Map<String, List<String>> KOREAN_REACTION_ALIASES = reactionAliases();

    private final DrugService drugService;
    private final SideEffectCacheService cacheService;
    private final EyakClient eyakClient;
    private final OpenFdaClient openFdaClient;

    public SideEffectService(DrugService drugService,
                             SideEffectCacheService cacheService,
                             EyakClient eyakClient,
                             OpenFdaClient openFdaClient) {
        this.drugService = drugService;
        this.cacheService = cacheService;
        this.eyakClient = eyakClient;
        this.openFdaClient = openFdaClient;
    }

    public DrugDtos.SideEffectsResponse getSideEffects(String drugId, String source) {
        Drug drug = drugService.findDrug(drugId);
        DrugDtos.SideEffectResult domestic = isOverseasOnly(source)
                ? null : loadDomestic(drug);
        DrugDtos.SideEffectResult overseas = isDomesticOnly(source)
                ? null : loadOverseas(drug, domestic);
        return new DrugDtos.SideEffectsResponse(domestic, overseas, FAERS_DISCLAIMER);
    }

    private DrugDtos.SideEffectResult loadDomestic(Drug drug) {
        return cacheService.findFresh(drug.getId(), DOMESTIC)
                .map(this::toDomesticResult)
                .orElseGet(() -> fetchAndCacheDomestic(drug));
    }

    private DrugDtos.SideEffectResult fetchAndCacheDomestic(Drug drug) {
        ExternalLookupResult externalResult = eyakClient.fetchDomesticSideEffects(drug);
        if (!externalResult.available()) {
            return new DrugDtos.SideEffectResult(false, List.of(), externalResult.message());
        }
        cacheService.save(drug.getId(), DOMESTIC, externalResult.items());
        return toDomesticResult(externalResult.items());
    }

    private DrugDtos.SideEffectResult loadOverseas(Drug drug, DrugDtos.SideEffectResult domestic) {
        ExternalCountLookupResult externalResult = openFdaClient.fetchOverseasSideEffectCounts(drug);
        if (!externalResult.available()) {
            return new DrugDtos.SideEffectResult(false, List.of(), externalResult.message());
        }
        String domesticCorpus = domestic == null ? "" : domestic.cases().stream()
                .map(DrugDtos.SideEffectCase::term)
                .reduce("", (left, right) -> left + " " + right);
        List<DrugDtos.SideEffectCase> cases = externalResult.items().stream()
                .map(item -> toOverseasCase(item, domesticCorpus))
                .toList();
        return new DrugDtos.SideEffectResult(true, cases, null);
    }

    private DrugDtos.SideEffectResult toDomesticResult(List<String> items) {
        List<DrugDtos.SideEffectCase> cases = items.stream()
                .map(item -> new DrugDtos.SideEffectCase(item, null, true))
                .toList();
        return new DrugDtos.SideEffectResult(true, cases, null);
    }

    private DrugDtos.SideEffectCase toOverseasCase(ExternalSideEffectCount item, String domesticCorpus) {
        return new DrugDtos.SideEffectCase(
                item.term(),
                item.count(),
                isMentionedInDomesticInformation(item.term(), domesticCorpus)
        );
    }

    private boolean isMentionedInDomesticInformation(String overseasTerm, String domesticCorpus) {
        List<String> aliases = KOREAN_REACTION_ALIASES.get(normalizeTerm(overseasTerm));
        return aliases != null && aliases.stream().anyMatch(domesticCorpus::contains);
    }

    private boolean isOverseasOnly(String source) {
        return OVERSEAS.equals(source);
    }

    private boolean isDomesticOnly(String source) {
        return DOMESTIC.equals(source);
    }

    private String normalizeTerm(String term) {
        return term == null ? "" : term.trim().toUpperCase(Locale.ROOT);
    }

    private static Map<String, List<String>> reactionAliases() {
        Map<String, List<String>> aliases = new LinkedHashMap<>();
        aliases.put("NAUSEA", List.of("구역"));
        aliases.put("VOMITING", List.of("구토"));
        aliases.put("DIZZINESS", List.of("어지러움"));
        aliases.put("RASH", List.of("발진"));
        aliases.put("FATIGUE", List.of("피로"));
        aliases.put("PYREXIA", List.of("발열"));
        aliases.put("HEADACHE", List.of("두통"));
        aliases.put("DIARRHOEA", List.of("설사"));
        aliases.put("DIARRHEA", List.of("설사"));
        aliases.put("PRURITUS", List.of("가려움"));
        aliases.put("URTICARIA", List.of("두드러기"));
        aliases.put("ANAPHYLACTIC REACTION", List.of("아나필락시스"));
        return Map.copyOf(aliases);
    }
}
