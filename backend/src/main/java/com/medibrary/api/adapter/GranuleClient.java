package com.medibrary.api.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Component
public class GranuleClient {
    private static final String ENDPOINT = "http://apis.data.go.kr/1471000/MdcinGrnIdntfcInfoService01/getMdcinGrnIdntfcInfoList01";

    private final String serviceKey;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public GranuleClient(@Value("${app.external.data-go-kr-service-key:}") String serviceKey,
                         ObjectMapper objectMapper,
                         ExternalRestClientFactory restClientFactory) {
        this.serviceKey = serviceKey;
        this.objectMapper = objectMapper;
        this.restClient = restClientFactory.create(ENDPOINT);
    }

    public List<ExternalDrug> search(String name, String shape, String color) {
        if (serviceKey.isBlank()) return List.of();
        try {
            String body = restClient.get().uri(uriBuilder -> {
                var builder = uriBuilder
                        .queryParam("serviceKey", serviceKey)
                        .queryParam("pageNo", 1)
                        .queryParam("numOfRows", 20)
                        .queryParam("type", "json");
                if (hasText(name)) builder.queryParam("item_name", name.trim());
                if (hasText(shape)) builder.queryParam("drug_shape", shape.trim());
                if (hasText(color)) builder.queryParam("color_class1", color.trim());
                return builder.build();
            }).retrieve().body(String.class);
            return extractItems(body);
        } catch (Exception ex) {
            return List.of();
        }
    }

    private List<ExternalDrug> extractItems(String responseBody) throws Exception {
        JsonNode items = objectMapper.readTree(responseBody).path("body").path("items").path("item");
        List<ExternalDrug> results = new ArrayList<>();
        if (items.isArray()) for (JsonNode item : items) addItem(item, results);
        else if (items.isObject()) addItem(items, results);
        return results;
    }

    private void addItem(JsonNode item, List<ExternalDrug> results) {
        String id = firstText(item, "ITEM_SEQ", "itemSeq");
        String name = firstText(item, "ITEM_NAME", "itemName");
        if (id.isBlank() || name.isBlank()) return;
        results.add(new ExternalDrug(
                id, name,
                firstText(item, "DRUG_SHAPE", "drugShape"),
                firstText(item, "COLOR_CLASS1", "colorClass1"),
                firstText(item, "PRINT_FRONT", "printFront"),
                firstText(item, "PRINT_BACK", "printBack"),
                firstText(item, "ITEM_IMAGE", "itemImage")
        ));
    }

    private String firstText(JsonNode node, String... keys) {
        for (String key : keys) {
            String value = node.path(key).asText("").trim();
            if (!value.isBlank()) return value;
        }
        return "";
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
