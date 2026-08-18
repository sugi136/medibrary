package com.medibrary.api.adapter;

import com.medibrary.api.entity.Drug;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class IngredientEnglishMapper {
    private static final Pattern PARENTHETICAL_INGREDIENT = Pattern.compile("\\(([^)]{1,120})\\)");
    private static final Map<String, String> KOREAN_TO_ENGLISH = createMappings();

    public Optional<String> resolve(Drug drug) {
        if (hasText(drug.getIngredientEn())) {
            return Optional.of(drug.getIngredientEn().trim());
        }
        return resolveFromText(drug.getName());
    }

    public Optional<String> resolveFromText(String text) {
        if (!hasText(text)) {
            return Optional.empty();
        }
        String ingredientCandidate = parentheticalIngredient(text).orElse(text);
        return KOREAN_TO_ENGLISH.entrySet().stream()
                .filter(entry -> ingredientCandidate.contains(entry.getKey()) || text.contains(entry.getKey()))
                .sorted((left, right) -> Integer.compare(right.getKey().length(), left.getKey().length()))
                .map(Map.Entry::getValue)
                .findFirst();
    }

    private Optional<String> parentheticalIngredient(String text) {
        Matcher matcher = PARENTHETICAL_INGREDIENT.matcher(text);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static Map<String, String> createMappings() {
        Map<String, String> mappings = new LinkedHashMap<>();
        mappings.put("아세트아미노펜", "ACETAMINOPHEN");
        mappings.put("이부프로펜", "IBUPROFEN");
        mappings.put("덱시부프로펜", "DEXIBUPROFEN");
        mappings.put("나프록센", "NAPROXEN");
        mappings.put("아스피린", "ASPIRIN");
        mappings.put("디클로페낙", "DICLOFENAC");
        mappings.put("메페남산", "MEFENAMIC ACID");
        mappings.put("클로르페니라민", "CHLORPHENIRAMINE");
        mappings.put("로라타딘", "LORATADINE");
        mappings.put("세티리진", "CETIRIZINE");
        mappings.put("펙소페나딘", "FEXOFENADINE");
        mappings.put("슈도에페드린", "PSEUDOEPHEDRINE");
        mappings.put("덱스트로메토르판", "DEXTROMETHORPHAN");
        mappings.put("구아이페네신", "GUAIFENESIN");
        mappings.put("에카베트나트륨수화물", "ECABEPIDE");
        mappings.put("에카베트나트륨", "ECABEPIDE");
        mappings.put("에스오메프라졸", "ESOMEPRAZOLE");
        mappings.put("오메프라졸", "OMEPRAZOLE");
        mappings.put("판토프라졸", "PANTOPRAZOLE");
        mappings.put("메트포르민", "METFORMIN");
        mappings.put("아토르바스타틴", "ATORVASTATIN");
        mappings.put("로수바스타틴", "ROSUVASTATIN");
        mappings.put("암로디핀", "AMLODIPINE");
        mappings.put("로사르탄", "LOSARTAN");
        mappings.put("발사르탄", "VALSARTAN");
        mappings.put("클로피도그렐", "CLOPIDOGREL");
        return Map.copyOf(mappings);
    }
}
