package com.medibrary.api.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medibrary.api.dto.DrugDtos;
import com.medibrary.api.entity.Drug;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

/**
 * openFDA NDC Directory 에서 국내 약과 동일 주성분인 미국 유통 제품을 조회한다.
 * 상세 화면 '해외에서는 이런 약' 탭(REQ-F-015)에서 사용한다.
 *
 * 엔드포인트: GET /drug/ndc.json?search=active_ingredients.name:("PARACETAMOL"+"ACETAMINOPHEN")
 * 인증키 불필요(키 없이 분당 40회, 무료 키 발급 시 240회).
 *
 * 실패 시 예외를 던지지 않고 available=false 결과를 돌려준다(REQ-N-001).
 */
@Component
public class OverseasProductClient {
    private static final int FETCH_LIMIT = 50;
    private static final int RESULT_LIMIT = 12;

    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final AtcIngredientResolver ingredientResolver;

    public OverseasProductClient(@Value("${app.external.openfda-base-url}") String baseUrl,
                                 ObjectMapper objectMapper,
                                 ExternalRestClientFactory restClientFactory,
                                 AtcIngredientResolver ingredientResolver) {
        this.objectMapper = objectMapper;
        this.restClient = restClientFactory.create(baseUrl);
        this.ingredientResolver = ingredientResolver;
    }

    public DrugDtos.OverseasProductResponse fetch(Drug drug) {
        List<String> terms = ingredientResolver.resolveSearchTerms(drug);
        if (terms.isEmpty()) {
            return DrugDtos.OverseasProductResponse.unavailable(
                    drug.getId(), null,
                    "이 약의 영문 주성분명을 확인할 수 없어 해외 제품을 찾지 못했습니다.");
        }

        String ingredientEn = terms.get(0);
        try {
            String body = request(ingredientResolver.toOrClause("active_ingredients.name", terms));
            List<DrugDtos.OverseasProduct> products = parse(body);
            if (products.isEmpty()) {
                return DrugDtos.OverseasProductResponse.empty(
                        drug.getId(), ingredientEn,
                        "미국에서 유통 중인 동일 성분 제품을 찾지 못했습니다.");
            }
            return DrugDtos.OverseasProductResponse.success(drug.getId(), ingredientEn, products);
        } catch (RestClientResponseException ex) {
            // openFDA 는 결과가 없을 때도 404 를 반환한다. 장애가 아니므로 정상 빈 결과로 처리한다.
            if (ex.getStatusCode().value() == 404) {
                return DrugDtos.OverseasProductResponse.empty(
                        drug.getId(), ingredientEn,
                        "미국에서 유통 중인 동일 성분 제품을 찾지 못했습니다.");
            }
            return DrugDtos.OverseasProductResponse.unavailable(
                    drug.getId(), ingredientEn,
                    "현재 해외 제품 정보를 불러올 수 없습니다.");
        } catch (Exception ex) {
            return DrugDtos.OverseasProductResponse.unavailable(
                    drug.getId(), ingredientEn,
                    "현재 해외 제품 정보를 불러올 수 없습니다.");
        }
    }

    private String request(String searchClause) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/drug/ndc.json")
                        .queryParam("search", searchClause)
                        .queryParam("limit", FETCH_LIMIT)
                        .build())
                .retrieve()
                .body(String.class);
    }

    /**
     * 같은 성분이라도 포장 단위마다 레코드가 나뉘어 중복이 많다.
     * (브랜드명 + 제형) 기준으로 중복을 제거하고, 브랜드명이 있는 완제품을 우선한다.
     */
    private List<DrugDtos.OverseasProduct> parse(String body) throws Exception {
        JsonNode results = objectMapper.readTree(body).path("results");
        LinkedHashSet<String> seen = new LinkedHashSet<>();
        List<DrugDtos.OverseasProduct> branded = new ArrayList<>();
        List<DrugDtos.OverseasProduct> generic = new ArrayList<>();

        for (JsonNode node : results) {
            String brand = text(node, "brand_name");
            String genericName = text(node, "generic_name");
            String displayName = !brand.isBlank() ? brand : genericName;
            if (displayName.isBlank()) {
                continue;
            }

            String dosageForm = text(node, "dosage_form");
            String key = (displayName + "|" + dosageForm).toUpperCase(Locale.ROOT);
            if (!seen.add(key)) {
                continue;
            }

            DrugDtos.OverseasProduct product = new DrugDtos.OverseasProduct(
                    displayName,
                    blankToNull(genericName),
                    blankToNull(text(node, "labeler_name")),
                    blankToNull(dosageForm),
                    blankToNull(firstOf(node, "route")),
                    blankToNull(activeIngredients(node)),
                    blankToNull(text(node, "product_ndc")),
                    "US");

            if (!brand.isBlank() && !brand.equalsIgnoreCase(genericName)) {
                branded.add(product);
            } else {
                generic.add(product);
            }
        }

        List<DrugDtos.OverseasProduct> merged = new ArrayList<>(branded);
        merged.addAll(generic);
        return merged.size() > RESULT_LIMIT ? List.copyOf(merged.subList(0, RESULT_LIMIT)) : List.copyOf(merged);
    }

    private String activeIngredients(JsonNode node) {
        List<String> parts = new ArrayList<>();
        for (JsonNode ingredient : node.path("active_ingredients")) {
            String name = ingredient.path("name").asText("").trim();
            String strength = ingredient.path("strength").asText("").trim();
            if (name.isBlank()) {
                continue;
            }
            parts.add(strength.isBlank() ? name : name + " " + strength);
        }
        return String.join(", ", parts);
    }

    private String firstOf(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (value.isArray() && !value.isEmpty()) {
            return value.get(0).asText("").trim();
        }
        return value.asText("").trim();
    }

    private String text(JsonNode node, String field) {
        return node.path(field).asText("").trim();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
