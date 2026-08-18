package com.medibrary.api.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medibrary.api.entity.Drug;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Component
public class DurClient {
    private static final String BASE_ENDPOINT = "https://apis.data.go.kr/1471000/DURPrdlstInfoService03";
    private static final String CONTRAINDICATION_OPERATION = "/getUsjntTabooInfoList03";
    private static final String EFFICACY_DUPLICATION_OPERATION = "/getEfcyDplctInfoList03";

    private final String serviceKey;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public DurClient(@Value("${app.external.data-go-kr-service-key:}") String serviceKey,
                     ObjectMapper objectMapper,
                     ExternalRestClientFactory restClientFactory) {
        this.serviceKey = serviceKey;
        this.objectMapper = objectMapper;
        this.restClient = restClientFactory.create(BASE_ENDPOINT);
    }

    public ExternalContraindicationResult fetch(Drug drug) {
        return fetchByOperation(drug, CONTRAINDICATION_OPERATION, "병용금기");
    }

    public ExternalContraindicationResult fetchEfficacyDuplicates(Drug drug) {
        return fetchByOperation(drug, EFFICACY_DUPLICATION_OPERATION, "효능군 중복");
    }

    private ExternalContraindicationResult fetchByOperation(Drug drug, String operation, String label) {
        if (serviceKey.isBlank()) {
            return ExternalContraindicationResult.unavailable("현재 " + label + " 정보를 불러올 수 없습니다");
        }
        try {
            String body = restClient.get().uri(uriBuilder -> uriBuilder
                    .path(operation)
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("itemSeq", drug.getId())
                    .queryParam("pageNo", 1)
                    .queryParam("numOfRows", 100)
                    .queryParam("type", "json")
                    .build())
                    .retrieve().body(String.class);
            return ExternalContraindicationResult.success(extractItems(body, label));
        } catch (Exception ex) {
            return ExternalContraindicationResult.unavailable("현재 " + label + " 정보를 불러올 수 없습니다");
        }
    }

    private List<ExternalContraindicationResult.Item> extractItems(String responseBody, String defaultReason) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode payload = root.path("response").isObject() ? root.path("response") : root;
        JsonNode items = payload.path("body").path("items");
        List<ExternalContraindicationResult.Item> results = new ArrayList<>();
        addItems(items, defaultReason, results);
        return results;
    }

    private void addItems(JsonNode items, String defaultReason, List<ExternalContraindicationResult.Item> results) {
        if (items.isArray()) {
            for (JsonNode item : items) addItem(item, defaultReason, results);
            return;
        }
        if (items.isObject()) {
            JsonNode wrappedItems = items.path("item");
            if (wrappedItems.isArray()) {
                for (JsonNode item : wrappedItems) addItem(item, defaultReason, results);
            } else if (wrappedItems.isObject()) {
                addItem(wrappedItems, defaultReason, results);
            } else {
                addItem(items, defaultReason, results);
            }
        }
    }

    private void addItem(JsonNode item, String defaultReason, List<ExternalContraindicationResult.Item> results) {
        String id = firstText(item, "MIXTURE_ITEM_SEQ", "mixItemSeq", "itemSeq2", "ITEM_SEQ", "itemSeq");
        String name = firstText(item, "MIXTURE_ITEM_NAME", "MIXTURE_ITEM_NM", "mixItemName", "ITEM_NAME", "itemName");
        String reason = firstText(item, "PROHBT_CONTENT", "PROHBT_REASON", "EFCY_DPLCT_REMARK", "REMARK", "reason");
        String rawType = firstText(item, "TYPE", "type", "PROHBT_TYPE", "EFCY_DPLCT_TYPE");
        String type = rawType.contains("주의") || rawType.equalsIgnoreCase("CAUTION") ? "CAUTION" : "CONTRAINDICATED";
        if (!name.isBlank()) {
            results.add(new ExternalContraindicationResult.Item(id, name, type,
                    reason.isBlank() ? defaultReason : reason));
        }
    }

    private String firstText(JsonNode node, String... keys) {
        for (String key : keys) {
            String value = node.path(key).asText("").trim();
            if (!value.isBlank()) return value;
        }
        return "";
    }
}
