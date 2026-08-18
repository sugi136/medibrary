package com.medibrary.api.service;

import com.medibrary.api.adapter.DurClient;
import com.medibrary.api.dto.DrugDtos;
import com.medibrary.api.service.cache.ContraindicationCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DurServiceTest {
    @Mock
    private DrugService drugService;

    @Mock
    private ContraindicationCacheService cacheService;

    @Mock
    private DurClient durClient;

    private DurService durService;

    @BeforeEach
    void setUp() {
        durService = new DurService(drugService, cacheService, durClient, 24);
    }

    @Test
    void cachedContraindicationsExist_thenReturnCacheWithoutExternalCall() {
        DrugDtos.ContraindicationItem cachedItem = new DrugDtos.ContraindicationItem(
                "drug-b", "상대 약", null, "CONTRAINDICATED", "병용 금기");
        when(cacheService.findFreshForDrug(eq("drug-a"), any(LocalDateTime.class))).thenReturn(List.of(cachedItem));

        DrugDtos.ContraindicationResponse response = durService.getContraindications("drug-a");

        assertThat(response.available()).isTrue();
        assertThat(response.items()).containsExactly(cachedItem);
        verify(durClient, never()).fetch(org.mockito.ArgumentMatchers.any());
    }
}
