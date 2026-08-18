package com.medibrary.api.service;

import com.medibrary.api.adapter.DurClient;
import com.medibrary.api.adapter.IngredientEnglishMapper;
import com.medibrary.api.dto.DrugDtos;
import com.medibrary.api.entity.Drug;
import com.medibrary.api.entity.Favorite;
import com.medibrary.api.repository.FavoriteRepository;
import com.medibrary.api.security.CurrentUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class DuplicateWarningService {
    private static final String SAME_INGREDIENT = "SAME_INGREDIENT";
    private static final String SAME_EFFICACY_GROUP = "SAME_EFFICACY_GROUP";

    private final DrugService drugService;
    private final FavoriteRepository favoriteRepository;
    private final CurrentUserProvider currentUserProvider;
    private final IngredientEnglishMapper ingredientEnglishMapper;
    private final DurClient durClient;

    public DuplicateWarningService(DrugService drugService,
                                   FavoriteRepository favoriteRepository,
                                   CurrentUserProvider currentUserProvider,
                                   IngredientEnglishMapper ingredientEnglishMapper,
                                   DurClient durClient) {
        this.drugService = drugService;
        this.favoriteRepository = favoriteRepository;
        this.currentUserProvider = currentUserProvider;
        this.ingredientEnglishMapper = ingredientEnglishMapper;
        this.durClient = durClient;
    }

    @Transactional(readOnly = true)
    public DrugDtos.DuplicateWarningResponse getWarnings(String drugId) {
        Drug drug = drugService.findDrug(drugId);
        List<DrugDtos.DuplicateWarningItem> sameIngredientItems = findSameIngredientFavorites(drug);
        var efficacyResult = durClient.fetchEfficacyDuplicates(drug);
        if (!efficacyResult.available()) {
            return new DrugDtos.DuplicateWarningResponse(
                    drugId, false, sameIngredientItems, List.of(),
                    "효능군 중복 정보를 불러올 수 없습니다."
            );
        }
        List<DrugDtos.DuplicateWarningItem> efficacyItems = efficacyResult.items().stream()
                .filter(item -> !drug.getId().equals(item.drugId()))
                .map(item -> new DrugDtos.DuplicateWarningItem(
                        item.drugId(), item.name(), SAME_EFFICACY_GROUP, item.reason()))
                .toList();
        return new DrugDtos.DuplicateWarningResponse(
                drugId, true, sameIngredientItems, efficacyItems, null
        );
    }

    private List<DrugDtos.DuplicateWarningItem> findSameIngredientFavorites(Drug drug) {
        Optional<String> currentIngredient = ingredientEnglishMapper.resolve(drug);
        if (currentIngredient.isEmpty()) {
            return List.of();
        }
        return currentUserProvider.getUserId()
                .map(userId -> favoriteRepository.findAllByUserIdOrderByCreatedAtDesc(userId))
                .orElseGet(List::of)
                .stream()
                .map(Favorite::getDrug)
                .filter(favoriteDrug -> !drug.getId().equals(favoriteDrug.getId()))
                .filter(favoriteDrug -> ingredientEnglishMapper.resolve(favoriteDrug)
                        .map(currentIngredient.get()::equalsIgnoreCase)
                        .orElse(false))
                .map(favoriteDrug -> new DrugDtos.DuplicateWarningItem(
                        favoriteDrug.getId(), favoriteDrug.getName(), SAME_INGREDIENT,
                        "즐겨찾기 약과 동일한 유효성분이 확인되었습니다."
                ))
                .toList();
    }
}
