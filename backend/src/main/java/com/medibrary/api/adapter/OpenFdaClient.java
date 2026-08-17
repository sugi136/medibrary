package com.medibrary.api.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medibrary.api.entity.Drug;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.LinkedHashSet;
import java.util.List;

@Component
public class OpenFdaClient {
    private static final int MAX_ATTEMPTS = 3;

    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final IngredientEnglishMapper ingredientEnglishMapper;

    public OpenFdaClient(@Value("${app.external.openfda-base-url}") String baseUrl,
                         ObjectMapper objectMapper,
                         ExternalRestClientFactory restClientFactory,
                         IngredientEnglishMapper ingredientEnglishMapper) {
        this.objectMapper = objectMapper;
        this.restClient = restClientFactory.create(baseUrl);
        this.ingredientEnglishMapper = ingredientEnglishMapper;
    }

    public ExternalLookupResult fetchOverseasSideEffects(Drug drug) {
        String ingredientEn = ingredientEnglishMapper.resolve(drug).orElse("");
        if (ingredientEn.isBlank()) {
            return ExternalLookupResult.unavailable("해외 부작용 정보를 위한 영문 성분명 매핑 정보가 없습니다.");
        }

        try {
            return ExternalLookupResult.success(extractReactions(requestEvents(ingredientEn)));
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                return ExternalLookupResult.success(List.of());
            }
            return ExternalLookupResult.unavailable("해외 부작용 정보를 일시적으로 불러올 수 없습니다.");
        } catch (Exception ex) {
            return ExternalLookupResult.unavailable("해외 부작용 정보를 일시적으로 불러올 수 없습니다.");
        }
    }

    private String requestEvents(String ingredientEn) {
        RuntimeException lastFailure = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return restClient.get().uri(uriBuilder -> uriBuilder
                        .path("/drug/event.json")
                        .queryParam("search", "patient.drug.medicinalproduct:\"" + ingredientEn + "\"")
                        .queryParam("limit", 5)
                        .build())
                        .retrieve().body(String.class);
            } catch (RuntimeException ex) {
                if (!isRetryable(ex) || attempt == MAX_ATTEMPTS) {
                    throw ex;
                }
                lastFailure = ex;
                pauseBeforeRetry(attempt);
            }
        }
        throw lastFailure == null ? new IllegalStateException("openFDA 요청을 완료하지 못했습니다.") : lastFailure;
    }

    private boolean isRetryable(RuntimeException ex) {
        if (ex instanceof ResourceAccessException) {
            return true;
        }
        if (ex instanceof RestClientResponseException responseException) {
            int status = responseException.getStatusCode().value();
            return status == 429 || status >= 500;
        }
        return false;
    }

    private void pauseBeforeRetry(int attempt) {
        try {
            Thread.sleep(250L * attempt);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("openFDA 재시도 대기 중 인터럽트되었습니다.", ex);
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
