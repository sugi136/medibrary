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
    private static final String ENDPOINT = "http://apis.data.go.kr/1471000/DURPrdlstInfoService03/getUsjntTabooInfoList03";

    private final String serviceKey;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public DurClient(@Value("${app.external.data-go-kr-service-key:}") String serviceKey,
                     ObjectMapper objectMapper,
                     ExternalRestClientFactory restClientFactory) {
        this.serviceKey = serviceKey;
        this.objectMapper = objectMapper;
        this.restClient = restClientFactory.create(ENDPOINT);
    }

    public ExternalContraindicationResult fetch(Drug drug) {
        if (serviceKey.isBlank()) {
            return ExternalContraindicationResult.unavailable("현재 병용금기 정보를 불러올 수 없습니다");
        }
        try {
            String body = restClient.get().uri(uriBuilder -> uriBuilder
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("itemSeq", drug.getId())
                    .queryParam("pageNo", 1)
                    .queryParam("numOfRows", 100)
                    .queryParam("type", "json")
                    .build())
                    .retrieve().body(String.class);
            return ExternalContraindicationResult.success(extractItems(body));
        } catch (Exception ex) {
            return ExternalContraindicationResult.unavailable("현재 병용금기 정보를 불러올 수 없습니다");
        }
    }

    private List<ExternalContraindicationResult.Item> extractItems(String responseBody) throws Exception {
        JsonNode items = objectMapper.readTree(responseBody).path("body").path("items").path("item");
        List<ExternalContraindicationResult.Item> results = new ArrayList<>();
        if (items.isArray()) {
            for (JsonNode item : items) addItem(item, results);
        } else if (items.isObject()) {
            addItem(items, results);
        }
        return results;
    }

    private void addItem(JsonNode item, List<ExternalContraindicationResult.Item> results) {
        String id = firstText(item, "MIXTURE_ITEM_SEQ", "mixItemSeq", "itemSeq2");
        String name = firstText(item, "MIXTURE_ITEM_NAME", "MIXTURE_ITEM_NM", "mixItemName");
        String reason = firstText(item, "PROHBT_CONTENT", "PROHBT_REASON", "reason");
        String rawType = firstText(item, "TYPE", "type", "PROHBT_TYPE");
        String type = rawType.contains("주의") || rawType.equalsIgnoreCase("CAUTION") ? "CAUTION" : "CONTRAINDICATED";
        if (!name.isBlank()) results.add(new ExternalContraindicationResult.Item(id, name, type, reason));
    }

    private String firstText(JsonNode node, String... keys) {
        for (String key : keys) {
            String value = node.path(key).asText("").trim();
            if (!value.isBlank()) return value;
        }
        return "";
    }
}
