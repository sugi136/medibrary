package com.medibrary.api.service;

import com.medibrary.api.adapter.DurClient;
import com.medibrary.api.adapter.ExternalContraindicationResult;
import com.medibrary.api.adapter.IngredientEnglishMapper;
import com.medibrary.api.dto.DrugDtos;
import com.medibrary.api.entity.Drug;
import com.medibrary.api.entity.Favorite;
import com.medibrary.api.repository.FavoriteRepository;
import com.medibrary.api.security.CurrentUserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class DuplicateWarningServiceTest {
    @Mock private DrugService drugService;
    @Mock private FavoriteRepository favoriteRepository;
    @Mock private CurrentUserProvider currentUserProvider;
    @Mock private DurClient durClient;
    @Mock private Favorite favorite;

    private DuplicateWarningService service;

    @BeforeEach
    void setUp() {
        service = new DuplicateWarningService(
                drugService,
                favoriteRepository,
                currentUserProvider,
                new IngredientEnglishMapper(),
                durClient
        );
    }

    @Test
    void returnsSameIngredientFavoriteAndEfficacyGroupWarnings() {
        Drug currentDrug = new Drug("current", "타이레놀정500밀리그램(아세트아미노펜)");
        Drug favoriteDrug = new Drug("favorite", "어린이타이레놀현탁액(아세트아미노펜)");
        given(drugService.findDrug("current")).willReturn(currentDrug);
        given(currentUserProvider.getUserId()).willReturn(Optional.of(1L));
        given(favoriteRepository.findAllByUserIdOrderByCreatedAtDesc(1L)).willReturn(List.of(favorite));
        given(favorite.getDrug()).willReturn(favoriteDrug);
        given(durClient.fetchEfficacyDuplicates(currentDrug)).willReturn(ExternalContraindicationResult.success(List.of(
                new ExternalContraindicationResult.Item("efficacy", "같은 효능군 후보약", "CAUTION", "효능군 중복 주의")
        )));

        DrugDtos.DuplicateWarningResponse result = service.getWarnings("current");

        assertThat(result.available()).isTrue();
        assertThat(result.sameIngredientItems()).singleElement()
                .extracting(DrugDtos.DuplicateWarningItem::name, DrugDtos.DuplicateWarningItem::category)
                .containsExactly("어린이타이레놀현탁액(아세트아미노펜)", "SAME_INGREDIENT");
        assertThat(result.efficacyGroupItems()).singleElement()
                .extracting(DrugDtos.DuplicateWarningItem::name, DrugDtos.DuplicateWarningItem::category)
                .containsExactly("같은 효능군 후보약", "SAME_EFFICACY_GROUP");
    }
}
