package com.medibrary.api.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medibrary.api.dto.DrugDtos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OverseasProductClientResponseTest {
    private OverseasProductClient overseasProductClient;
    private Method parse;

    @BeforeEach
    void setUp() throws Exception {
        AtcIngredientResolver resolver = new AtcIngredientResolver(new IngredientEnglishMapper());
        resolver.loadResources();
        overseasProductClient = new OverseasProductClient(
                "https://api.fda.gov",
                new ObjectMapper(),
                new ExternalRestClientFactory(1_000),
                resolver
        );
        parse = OverseasProductClient.class.getDeclaredMethod("parse", String.class);
        parse.setAccessible(true);
    }

    @Test
    void prioritizesBrandedProductsAndDeduplicatesSameNameAndDosageForm() throws Exception {
        List<DrugDtos.OverseasProduct> products = parse("""
                {
                  "results": [
                    {
                      "brand_name": "TYLENOL",
                      "generic_name": "ACETAMINOPHEN",
                      "labeler_name": "Kenvue Brands LLC",
                      "dosage_form": "TABLET",
                      "route": ["ORAL"],
                      "active_ingredients": [{"name": "ACETAMINOPHEN", "strength": "500 mg"}],
                      "product_ndc": "50580-449"
                    },
                    {
                      "brand_name": "TYLENOL",
                      "generic_name": "ACETAMINOPHEN",
                      "dosage_form": "TABLET",
                      "product_ndc": "50580-450"
                    },
                    {
                      "generic_name": "ACETAMINOPHEN",
                      "dosage_form": "CAPSULE",
                      "product_ndc": "00093-1040"
                    }
                  ]
                }
                """);

        assertThat(products).hasSize(2);
        assertThat(products.get(0))
                .extracting(DrugDtos.OverseasProduct::name,
                        DrugDtos.OverseasProduct::activeIngredients,
                        DrugDtos.OverseasProduct::country)
                .containsExactly("TYLENOL", "ACETAMINOPHEN 500 mg", "US");
        assertThat(products.get(1).name()).isEqualTo("ACETAMINOPHEN");
    }

    @SuppressWarnings("unchecked")
    private List<DrugDtos.OverseasProduct> parse(String responseBody) throws Exception {
        return (List<DrugDtos.OverseasProduct>) parse.invoke(overseasProductClient, responseBody);
    }
}
