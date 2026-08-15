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
    private static final String ENDPOINT = "https://apis.data.go.kr/1471000/DrbEasyDrugInfoService/getDrbEasyDrugList";

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

    public ExternalDrugInformation fetchDrugInformation(Drug drug) {
        if (serviceKey.isBlank()) {
            return ExternalDrugInformation.unavailable("국내 의약품 정보 연동 키가 설정되지 않았습니다.");
        }
        try {
            String body = restClient.get().uri(uriBuilder -> uriBuilder
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("itemSeq", drug.getId())
                    .queryParam("itemName", drug.getName())
                    .queryParam("pageNo", 1)
                    .queryParam("numOfRows", 1)
                    .queryParam("type", "json")
                    .build())
                    .retrieve().body(String.class);
            return extractDrugInformation(body);
        } catch (Exception ex) {
            return ExternalDrugInformation.unavailable("현재 국내 의약품 정보를 불러올 수 없습니다.");
        }
    }

    public ExternalLookupResult fetchDomesticSideEffects(Drug drug) {
        ExternalDrugInformation information = fetchDrugInformation(drug);
        return information.available()
                ? ExternalLookupResult.success(information.sideEffects())
                : ExternalLookupResult.unavailable(information.message());
    }

    private ExternalDrugInformation extractDrugInformation(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        if (root.path("OpenAPI_ServiceResponse").path("cmmMsgHeader").isObject()) {
            return ExternalDrugInformation.unavailable("국내 의약품 정보 요청이 거절되었습니다.");
        }

        JsonNode payload = root.path("response").isObject() ? root.path("response") : root;
        JsonNode item = firstItem(payload.path("body").path("items"));
        if (item.isMissingNode() || !item.isObject()) {
            return ExternalDrugInformation.success("", "", "", List.of());
        }

        String efficacy = cleanText(firstText(item, "efcyQesitm", "EFCY_QESITM"));
        String usageInfo = cleanText(firstText(item, "useMethodQesitm", "USE_METHOD_QESITM"));
        String caution = joinNonBlank(
                cleanText(firstText(item, "atpnWarnQesitm", "ATPN_WARN_QESITM")),
                cleanText(firstText(item, "atpnQesitm", "ATPN_QESITM"))
        );
        String sideEffect = cleanText(firstText(item, "seQesitm", "SE_QESITM", "seQ", "SE_Q"));
        List<String> sideEffects = sideEffect.isBlank() ? List.of() : List.of(sideEffect);
        return ExternalDrugInformation.success(efficacy, usageInfo, caution, sideEffects);
    }

    private JsonNode firstItem(JsonNode items) {
        if (items.isArray() && !items.isEmpty()) return unwrapItem(items.get(0));
        if (items.isObject()) {
            JsonNode wrappedItems = items.path("item");
            if (wrappedItems.isArray() && !wrappedItems.isEmpty()) return unwrapItem(wrappedItems.get(0));
            if (wrappedItems.isObject()) return unwrapItem(wrappedItems);
            return items;
        }
        return items;
    }

    private JsonNode unwrapItem(JsonNode item) {
        return item.isObject() && item.size() == 1 && item.path("item").isObject()
                ? item.path("item")
                : item;
    }

    private String firstText(JsonNode item, String... keys) {
        for (String key : keys) {
            String value = item.path(key).asText("").trim();
            if (!value.isBlank()) return value;
        }
        return "";
    }

    private String cleanText(String value) {
        return value
                .replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("(?i)</p>", "\n")
                .replaceAll("<[^>]*>", " ")
                .replaceAll("[\\t ]+", " ")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }

    private String joinNonBlank(String first, String second) {
        if (first.isBlank()) return second;
        if (second.isBlank()) return first;
        return first + "\n\n" + second;
    }
}
