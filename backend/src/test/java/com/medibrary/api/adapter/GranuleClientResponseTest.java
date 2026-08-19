package com.medibrary.api.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class GranuleClientResponseTest {
    private GranuleClient granuleClient;
    private Method extractResult;

    @BeforeEach
    void setUp() throws Exception {
        granuleClient = new GranuleClient(
                "test-service-key",
                new ObjectMapper(),
                new ExternalRestClientFactory(1_000)
        );
        extractResult = GranuleClient.class.getDeclaredMethod("extractResult", String.class, int.class, int.class);
        extractResult.setAccessible(true);
    }

    @Test
    void extractsPagedItemsFromResponseWrappedArray() throws Exception {
        GranuleClient.SearchResult result = extract("""
                {
                  "response": {
                    "header": {"resultCode": "00", "resultMsg": "NORMAL SERVICE"},
                    "body": {
                      "totalCount": 42,
                      "items": [{
                        "ITEM_SEQ": "202106092",
                        "ITEM_NAME": "타이레놀정500밀리그람",
                        "DRUG_SHAPE": "장방형",
                        "COLOR_CLASS1": "하양",
                        "PRINT_FRONT": "TYLENOL",
                        "PRINT_BACK": "500",
                        "ITEM_IMAGE": "https://example.test/tylenol.jpg"
                      }]
                    }
                  }
                }
                """, 1, 20);

        assertThat(result.available()).isTrue();
        assertThat(result.totalCount()).isEqualTo(42);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.items()).singleElement().satisfies(drug -> {
            assertThat(drug.id()).isEqualTo("202106092");
            assertThat(drug.name()).isEqualTo("타이레놀정500밀리그람");
            assertThat(drug.shape()).isEqualTo("장방형");
            assertThat(drug.color()).isEqualTo("하양");
        });
    }

    @Test
    void extractsItemsFromLegacyItemWrapperWithoutTotalCount() throws Exception {
        GranuleClient.SearchResult result = extract("""
                {
                  "body": {
                    "items": {
                      "item": {
                        "ITEM_SEQ": "100",
                        "ITEM_NAME": "테스트정"
                      }
                    }
                  }
                }
                """, 1, 20);

        assertThat(result.available()).isTrue();
        assertThat(result.totalCount()).isEqualTo(1);
        assertThat(result.hasNext()).isFalse();
        assertThat(result.items()).singleElement().satisfies(drug -> {
            assertThat(drug.id()).isEqualTo("100");
            assertThat(drug.name()).isEqualTo("테스트정");
        });
    }

    private GranuleClient.SearchResult extract(String responseBody, int pageNo, int pageSize) throws Exception {
        return (GranuleClient.SearchResult) extractResult.invoke(granuleClient, responseBody, pageNo, pageSize);
    }
}
