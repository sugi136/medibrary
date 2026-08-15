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
public class EyakClient {
    private static final String ENDPOINT = "http://apis.data.go.kr/1471000/DrbEasyDrugInfoService/getDrbEasyDrugList";

    private final String serviceKey;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public EyakClient(@Value("${app.external.data-go-kr-service-key:}") String serviceKey,
                      ObjectMapper objectMapper,
                      ExternalRestClientFactory restClientFactory) {
        this.serviceKey = serviceKey;
        this.objectMapper = objectMapper;
        this.restClient = restClientFactory.create(ENDPOINT);
    }

    public ExternalLookupResult fetchDomesticSideEffects(Drug drug) {
        if (serviceKey.isBlank()) {
            return ExternalLookupResult.unavailable("국내 부작용 연동 키가 설정되지 않았습니다.");
        }
        try {
            String body = restClient.get().uri(uriBuilder -> uriBuilder
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("itemName", drug.getName())
                    .queryParam("pageNo", 1)
                    .queryParam("numOfRows", 1)
                    .queryParam("type", "json")
                    .build())
                    .retrieve().body(String.class);
            return ExternalLookupResult.success(extractSideEffects(body));
        } catch (Exception ex) {
            return ExternalLookupResult.unavailable("현재 정보를 불러올 수 없습니다.");
        }
    }

    private List<String> extractSideEffects(String responseBody) throws Exception {
        JsonNode items = objectMapper.readTree(responseBody).path("body").path("items").path("item");
        List<String> results = new ArrayList<>();
        if (items.isArray()) {
            for (JsonNode item : items) addSideEffect(item, results);
        } else if (items.isObject()) {
            addSideEffect(items, results);
        }
        return results;
    }

    private void addSideEffect(JsonNode item, List<String> results) {
        String raw = item.path("seQ").asText("").replaceAll("<[^>]*>", " ").trim();
        if (!raw.isBlank()) results.add(raw);
    }
}
