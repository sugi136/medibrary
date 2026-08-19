package com.medibrary.api.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.ArrayList;
import java.util.List;

@Component
public class GranuleClient {
    public record SearchResult(List<ExternalDrug> items, long totalCount, boolean hasNext, boolean available) {
        static SearchResult unavailable() {
            return new SearchResult(List.of(), 0, false, false);
        }
    }

    private static final Logger log = LoggerFactory.getLogger(GranuleClient.class);
    private static final String ENDPOINT =
            "https://apis.data.go.kr/1471000/MdcinGrnIdntfcInfoService03/getMdcinGrnIdntfcInfoList03";

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

    public SearchResult search(String name, String shape, String color, int pageNo, int pageSize) {
        if (serviceKey.isBlank()) {
            log.warn("낱알식별 API 호출을 건너뜁니다. DATA_GO_KR_SERVICE_KEY가 설정되지 않았습니다.");
            return SearchResult.unavailable();
        }

        try {
            String body = restClient.get().uri(uriBuilder -> {
                var builder = uriBuilder
                        .queryParam("serviceKey", serviceKey)
                        .queryParam("pageNo", pageNo)
                        .queryParam("numOfRows", pageSize)
                        .queryParam("type", "json");
                if (hasText(name)) builder.queryParam("item_name", name.trim());
                if (hasText(shape)) builder.queryParam("drug_shape", shape.trim());
                if (hasText(color)) builder.queryParam("color_class1", color.trim());
                return builder.build();
            }).retrieve().body(String.class);

            SearchResult result = extractResult(body, pageNo, pageSize);
            if (result.items().isEmpty()) {
                log.info("낱알식별 API 검색 결과가 없습니다. name='{}', shape='{}', color='{}', page={}",
                        safeQueryValue(name), safeQueryValue(shape), safeQueryValue(color), pageNo);
            }
            return result;
        } catch (RestClientResponseException ex) {
            log.warn("낱알식별 API HTTP 오류. status={}, apiError={}",
                    ex.getStatusCode().value(), extractApiErrorSummary(ex.getResponseBodyAsString()));
            return SearchResult.unavailable();
        } catch (Exception ex) {
            log.warn("낱알식별 API 호출 실패. exceptionType={}, message={}",
                    ex.getClass().getSimpleName(), safeExceptionMessage(ex.getMessage()));
            return SearchResult.unavailable();
        }
    }

    private SearchResult extractResult(String responseBody, int pageNo, int pageSize) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode errorHeader = root.path("OpenAPI_ServiceResponse").path("cmmMsgHeader");
        if (!errorHeader.isMissingNode() && errorHeader.isObject()) {
            log.warn("낱알식별 API가 요청을 거절했습니다. apiError={}", extractApiErrorSummary(root));
            return SearchResult.unavailable();
        }

        JsonNode payload = root.path("response").isObject() ? root.path("response") : root;
        JsonNode body = payload.path("body");
        List<ExternalDrug> results = new ArrayList<>();
        addItems(body.path("items"), results);

        long totalCount = body.path("totalCount").asLong(results.size());
        boolean hasNext = totalCount > (long) pageNo * pageSize;
        return new SearchResult(results, Math.max(totalCount, results.size()), hasNext, true);
    }

    /**
     * 공공데이터포털 응답의 두 가지 items 형식({@code [...]}, {@code {"item": [...]}})을 모두 수용한다.
     */
    private void addItems(JsonNode items, List<ExternalDrug> results) {
        if (items.isArray()) {
            for (JsonNode item : items) addWrappedOrDirectItem(item, results);
            return;
        }
        if (items.isObject()) {
            JsonNode wrappedItems = items.path("item");
            if (wrappedItems.isArray()) {
                for (JsonNode item : wrappedItems) addItem(item, results);
            } else if (wrappedItems.isObject()) {
                addItem(wrappedItems, results);
            } else {
                addItem(items, results);
            }
        }
    }

    private void addWrappedOrDirectItem(JsonNode item, List<ExternalDrug> results) {
        JsonNode wrappedItem = item.path("item");
        if (item.isObject() && item.size() == 1 && wrappedItem.isObject()) {
            addItem(wrappedItem, results);
            return;
        }
        addItem(item, results);
    }

    private String extractApiErrorSummary(String responseBody) {
        try {
            return extractApiErrorSummary(objectMapper.readTree(responseBody));
        } catch (Exception ignored) {
            return "응답 본문을 구조화해 읽을 수 없습니다";
        }
    }

    private String extractApiErrorSummary(JsonNode root) {
        JsonNode header = root.path("OpenAPI_ServiceResponse").path("cmmMsgHeader");
        String reasonCode = firstText(header, "returnReasonCode");
        String authMessage = firstText(header, "returnAuthMsg");
        String errorMessage = firstText(header, "errMsg");
        return "reasonCode=" + safeLogValue(reasonCode)
                + ", authMessage=" + safeLogValue(authMessage)
                + ", errorMessage=" + safeLogValue(errorMessage);
    }

    private void addItem(JsonNode item, List<ExternalDrug> results) {
        String id = firstText(item, "ITEM_SEQ", "itemSeq");
        String name = firstText(item, "ITEM_NAME", "itemName");
        if (id.isBlank() || name.isBlank()) return;
        results.add(new ExternalDrug(
                id, name,
                firstText(item, "ENTP_NAME", "entpName", "COMPANY_NAME", "companyName"),
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

    private String safeQueryValue(String value) {
        return safeLogValue(value == null ? "" : value.trim());
    }

    private String safeExceptionMessage(String value) {
        return safeLogValue(value == null ? "" : value.replace(serviceKey, "[REDACTED]"));
    }

    private String safeLogValue(String value) {
        if (value == null || value.isBlank()) return "-";
        String normalized = value.replaceAll("[\\r\\n\\t]+", " ").trim();
        return normalized.length() > 200 ? normalized.substring(0, 200) + "..." : normalized;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
