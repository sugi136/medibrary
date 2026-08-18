package com.medibrary.api.adapter;

import com.medibrary.api.entity.Drug;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * 국내 의약품을 openFDA 검색에 사용할 영문 성분명으로 해석한다.
 *
 * 해석 순서
 *  1) drugs.ingredient_en 이 이미 있으면 그대로 사용
 *  2) drugs.atc_code -> WHO ATC 5단계 성분명 (atc-ingredient-en.csv, 5,154건)
 *  3) 기존 한글 성분명 사전 (IngredientEnglishMapper) 폴백
 *
 * ATC 는 WHO 국제 표준이라 국내 허가 의약품과 미국 제품이 같은 코드를 공유한다.
 * 따라서 ATC 만 확보되면 영문 성분명은 결정적으로(deterministic) 구해진다.
 *
 * 다만 ATC 명칭은 WHO INN 기준이고 openFDA 는 미국 통용명(USAN) 기준이라
 * paracetamol/acetaminophen 처럼 표기가 갈리는 성분이 있다.
 * inn-usan-alias.csv 로 별칭을 함께 반환해 OR 검색에 사용한다.
 */
@Component
public class AtcIngredientResolver {
    private static final Logger log = LoggerFactory.getLogger(AtcIngredientResolver.class);

    private static final String ATC_RESOURCE = "ingredient/atc-ingredient-en.csv";
    private static final String ALIAS_RESOURCE = "ingredient/inn-usan-alias.csv";

    private final IngredientEnglishMapper legacyMapper;

    private Map<String, String> atcToIngredient = Map.of();
    private Map<String, List<String>> innToAliases = Map.of();

    public AtcIngredientResolver(IngredientEnglishMapper legacyMapper) {
        this.legacyMapper = legacyMapper;
    }

    @PostConstruct
    void loadResources() {
        // atc_code|ingredient_en|is_combination
        atcToIngredient = readTable(ATC_RESOURCE, columns -> columns.length >= 2
                ? Map.entry(columns[0].toUpperCase(Locale.ROOT), columns[1])
                : null);

        // inn|alias1|alias2...
        Map<String, List<String>> aliases = new LinkedHashMap<>();
        for (String line : readLines(ALIAS_RESOURCE)) {
            String[] columns = splitColumns(line);
            if (columns.length < 2) {
                continue;
            }
            List<String> values = new ArrayList<>();
            for (int i = 1; i < columns.length; i++) {
                if (hasText(columns[i])) {
                    values.add(columns[i]);
                }
            }
            if (!values.isEmpty()) {
                aliases.putIfAbsent(columns[0].toLowerCase(Locale.ROOT), List.copyOf(values));
            }
        }
        innToAliases = Map.copyOf(aliases);

        log.info("성분명 매핑 로드 완료 - ATC {}건, INN 별칭 {}건", atcToIngredient.size(), innToAliases.size());
    }

    /** openFDA 검색에 사용할 성분명 후보. 첫 번째가 대표값이며 나머지는 미국 통용명 별칭이다. */
    public List<String> resolveSearchTerms(Drug drug) {
        Optional<String> primary = resolvePrimary(drug);
        if (primary.isEmpty()) {
            return List.of();
        }
        String base = primary.get();
        LinkedHashSet<String> terms = new LinkedHashSet<>();
        terms.add(base);
        terms.addAll(innToAliases.getOrDefault(base.toLowerCase(Locale.ROOT), List.of()));
        return List.copyOf(terms);
    }

    /** 대표 영문 성분명 하나. 화면 표시용. */
    public Optional<String> resolvePrimary(Drug drug) {
        if (hasText(drug.getIngredientEn())) {
            return Optional.of(drug.getIngredientEn().trim());
        }
        if (hasText(drug.getAtcCode())) {
            String name = atcToIngredient.get(drug.getAtcCode().trim().toUpperCase(Locale.ROOT));
            if (hasText(name)) {
                return Optional.of(name);
            }
        }
        return legacyMapper.resolve(drug);
    }

    /** openFDA search 파라미터용 OR 절. 예: ("PARACETAMOL"+"ACETAMINOPHEN") */
    public String toOrClause(String field, List<String> terms) {
        List<String> quoted = new ArrayList<>();
        for (String term : terms) {
            quoted.add("\"" + term.replace("\"", "") + "\"");
        }
        return field + ":(" + String.join("+", quoted) + ")";
    }

    private Map<String, String> readTable(String resourcePath, LineParser parser) {
        Map<String, String> result = new LinkedHashMap<>();
        for (String line : readLines(resourcePath)) {
            Map.Entry<String, String> entry = parser.parse(splitColumns(line));
            if (entry != null && hasText(entry.getKey()) && hasText(entry.getValue())) {
                result.putIfAbsent(entry.getKey(), entry.getValue());
            }
        }
        return Map.copyOf(result);
    }

    /** 주석(#), 빈 줄, 헤더 1행을 걷어낸 데이터 행만 돌려준다. */
    private List<String> readLines(String resourcePath) {
        List<String> lines = new ArrayList<>();
        ClassPathResource resource = new ClassPathResource(resourcePath);
        if (!resource.exists()) {
            log.warn("성분명 매핑 리소스를 찾을 수 없습니다: {}", resourcePath);
            return List.of();
        }
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean headerSkipped = false;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.strip();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                if (!headerSkipped) {
                    headerSkipped = true;
                    continue;
                }
                lines.add(trimmed);
            }
        } catch (Exception ex) {
            log.error("성분명 매핑 리소스 로드 실패: {}", resourcePath, ex);
            return List.of();
        }
        return lines;
    }

    /** 구분자는 파이프. 성분명에 쉼표가 들어가는 항목이 450여 건 있어 CSV 를 쓰지 않는다. */
    private String[] splitColumns(String line) {
        String[] columns = line.split("\\|", -1);
        for (int i = 0; i < columns.length; i++) {
            columns[i] = columns[i].strip();
        }
        return columns;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    @FunctionalInterface
    private interface LineParser {
        Map.Entry<String, String> parse(String[] parts);
    }
}
