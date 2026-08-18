package com.medibrary.api.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medibrary.api.entity.Drug;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

@Component
public class OpenFdaClient {
    private static final int MAX_ATTEMPTS = 3;
    private static final int TOP_REACTION_LIMIT = 10;

    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final AtcIngredientResolver ingredientResolver;

    public OpenFdaClient(@Value("${app.external.openfda-base-url}") String baseUrl,
                         ObjectMapper objectMapper,
                         ExternalRestClientFactory restClientFactory,
                         AtcIngredientResolver ingredientResolver) {
        this.objectMapper = objectMapper;
        this.restClient = restClientFactory.create(baseUrl);
        this.ingredientResolver = ingredientResolver;
    }

    public ExternalCountLookupResult fetchOverseasSideEffectCounts(Drug drug) {
        String ingredientEn = ingredientResolver.resolvePrimary(drug).orElse("");
        if (ingredientEn.isBlank()) {
            return ExternalCountLookupResult.unavailable("해외 부작용 정보를 위한 영문 성분명 매핑 정보가 없습니다.");
        }

        try {
            return ExternalCountLookupResult.success(extractReactionCounts(requestReactionCounts(ingredientEn)));
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                return ExternalCountLookupResult.success(List.of());
            }
            return ExternalCountLookupResult.unavailable("해외 부작용 정보를 일시적으로 불러올 수 없습니다.");
        } catch (Exception ex) {
            return ExternalCountLookupResult.unavailable("해외 부작용 정보를 일시적으로 불러올 수 없습니다.");
        }
    }

    private String requestReactionCounts(String ingredientEn) {
        RuntimeException lastFailure = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return restClient.get().uri(uriBuilder -> uriBuilder
                        .path("/drug/event.json")
                        .queryParam("search", "patient.drug.medicinalproduct:\"" + ingredientEn + "\"")
                        .queryParam("count", "patient.reaction.reactionmeddrapt.exact")
                        .queryParam("limit", TOP_REACTION_LIMIT)
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

    private List<ExternalSideEffectCount> extractReactionCounts(String responseBody) throws Exception {
        JsonNode results = objectMapper.readTree(responseBody).path("results");
        if (!results.isArray()) {
            return List.of();
        }
        return java.util.stream.StreamSupport.stream(results.spliterator(), false)
                .map(item -> new ExternalSideEffectCount(
                        item.path("term").asText("").trim(),
                        item.path("count").asLong(0)
                ))
                .filter(item -> !item.term().isBlank())
                .limit(TOP_REACTION_LIMIT)
                .toList();
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
}
