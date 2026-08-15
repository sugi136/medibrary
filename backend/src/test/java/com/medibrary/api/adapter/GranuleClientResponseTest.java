package com.medibrary.api.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GranuleClientResponseTest {
    private GranuleClient granuleClient;
    private Method extractItems;

    @BeforeEach
    void setUp() throws Exception {
        granuleClient = new GranuleClient(
                "test-service-key",
                new ObjectMapper(),
                new ExternalRestClientFactory(1_000)
        );
        extractItems = GranuleClient.class.getDeclaredMethod("extractItems", String.class);
        extractItems.setAccessible(true);
    }

    @Test
    void extractsItemsFromResponseWrappedArray() throws Exception {
        List<ExternalDrug> results = extract("""
                {
                  "response": {
                    "header": {"resultCode": "00", "resultMsg": "NORMAL SERVICE"},
                    "body": {
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
                """);

        assertThat(results).singleElement().satisfies(drug -> {
            assertThat(drug.id()).isEqualTo("202106092");
            assertThat(drug.name()).isEqualTo("타이레놀정500밀리그람");
            assertThat(drug.shape()).isEqualTo("장방형");
            assertThat(drug.color()).isEqualTo("하양");
        });
    }

    @Test
    void extractsItemsFromLegacyItemWrapper() throws Exception {
        List<ExternalDrug> results = extract("""
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
                """);

        assertThat(results).singleElement().satisfies(drug -> {
            assertThat(drug.id()).isEqualTo("100");
            assertThat(drug.name()).isEqualTo("테스트정");
        });
    }

    @SuppressWarnings("unchecked")
    private List<ExternalDrug> extract(String responseBody) throws Exception {
        return (List<ExternalDrug>) extractItems.invoke(granuleClient, responseBody);
    }
}
