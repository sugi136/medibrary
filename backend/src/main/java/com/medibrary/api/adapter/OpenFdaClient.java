package com.medibrary.api.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medibrary.api.entity.Drug;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashSet;
import java.util.List;

@Component
public class OpenFdaClient {
    private final String baseUrl;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public OpenFdaClient(@Value("${app.external.openfda-base-url}") String baseUrl,
                         ObjectMapper objectMapper,
                         ExternalRestClientFactory restClientFactory) {
        this.baseUrl = baseUrl;
        this.objectMapper = objectMapper;
        this.restClient = restClientFactory.create(baseUrl);
    }

    public ExternalLookupResult fetchOverseasSideEffects(Drug drug) {
        if (drug.getIngredientEn() == null || drug.getIngredientEn().isBlank()) {
            return ExternalLookupResult.unavailable("해외 부작용 정보를 위한 영문 성분명 매핑 정보가 없습니다.");
        }
        try {
            String body = restClient.get().uri(uriBuilder -> uriBuilder
                    .path("/drug/event.json")
                    .queryParam("search", "patient.drug.medicinalproduct:\"" + drug.getIngredientEn() + "\"")
                    .queryParam("limit", 5)
                    .build())
                    .retrieve().body(String.class);
            return ExternalLookupResult.success(extractReactions(body));
        } catch (Exception ex) {
            return ExternalLookupResult.unavailable("현재 정보를 불러올 수 없습니다.");
        }
    }

    private List<String> extractReactions(String responseBody) throws Exception {
        LinkedHashSet<String> reactions = new LinkedHashSet<>();
        JsonNode results = objectMapper.readTree(responseBody).path("results");
        for (JsonNode result : results) {
            for (JsonNode reaction : result.path("patient").path("reaction")) {
                String term = reaction.path("reactionmeddrapt").asText("").trim();
                if (!term.isBlank()) reactions.add(term);
            }
        }
        return reactions.stream().limit(10).toList();
    }
}
