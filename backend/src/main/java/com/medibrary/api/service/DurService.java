package com.medibrary.api.service;

import com.medibrary.api.adapter.DurClient;
import com.medibrary.api.dto.DrugDtos;
import com.medibrary.api.dto.FavoriteDtos;
import com.medibrary.api.entity.Drug;
import com.medibrary.api.service.cache.ContraindicationCacheService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DurService {
    private final DrugService drugService;
    private final ContraindicationCacheService cacheService;
    private final DurClient durClient;

    public DurService(DrugService drugService,
                      ContraindicationCacheService cacheService,
                      DurClient durClient) {
        this.drugService = drugService;
        this.cacheService = cacheService;
        this.durClient = durClient;
    }

    public DrugDtos.ContraindicationResponse getContraindications(String drugId) {
        List<DrugDtos.ContraindicationItem> cachedItems = cacheService.findForDrug(drugId);
        if (!cachedItems.isEmpty()) {
            return new DrugDtos.ContraindicationResponse(drugId, true, cachedItems, null);
        }

        Drug drug = drugService.findDrug(drugId);
        var externalResult = durClient.fetch(drug);
        if (!externalResult.available()) {
            return unavailableResponse(drugId);
        }
        if (externalResult.items().isEmpty()) {
            return emptyResponse(drugId);
        }
        List<DrugDtos.ContraindicationItem> items = externalResult.items().stream()
                .map(item -> new DrugDtos.ContraindicationItem(
                        item.drugId(), item.name(), null, item.type(), item.reason()))
                .toList();
        return new DrugDtos.ContraindicationResponse(drugId, true, items, null);
    }

    public List<FavoriteDtos.DurWarning> checkFavoritePairs(List<String> drugIds) {
        return cacheService.findWarningsWithin(drugIds);
    }

    private DrugDtos.ContraindicationResponse unavailableResponse(String drugId) {
        return new DrugDtos.ContraindicationResponse(
                drugId, false, List.of(), "현재 병용금기 정보를 불러올 수 없습니다");
    }

    private DrugDtos.ContraindicationResponse emptyResponse(String drugId) {
        return new DrugDtos.ContraindicationResponse(
                drugId, true, List.of(), "확인된 병용금기 정보가 없습니다");
    }
}
