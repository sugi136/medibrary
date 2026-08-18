package com.medibrary.api.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OpenFdaClientResponseTest {
    private OpenFdaClient openFdaClient;
    private Method extractReactionCounts;

    @BeforeEach
    void setUp() throws Exception {
        openFdaClient = new OpenFdaClient(
                "https://api.fda.gov",
                new ObjectMapper(),
                new ExternalRestClientFactory(1_000),
                new AtcIngredientResolver(new IngredientEnglishMapper())
        );
        extractReactionCounts = OpenFdaClient.class.getDeclaredMethod("extractReactionCounts", String.class);
        extractReactionCounts.setAccessible(true);
    }

    @Test
    void extractsTermsAndCountsFromFaersAggregationResponse() throws Exception {
        List<ExternalSideEffectCount> results = extract("""
                {
                  "results": [
                    {"term": "NAUSEA", "count": 26806},
                    {"term": "VOMITING", "count": 18004}
                  ]
                }
                """);

        assertThat(results).containsExactly(
                new ExternalSideEffectCount("NAUSEA", 26806),
                new ExternalSideEffectCount("VOMITING", 18004)
        );
    }

    @SuppressWarnings("unchecked")
    private List<ExternalSideEffectCount> extract(String responseBody) throws Exception {
        return (List<ExternalSideEffectCount>) extractReactionCounts.invoke(openFdaClient, responseBody);
    }
}
