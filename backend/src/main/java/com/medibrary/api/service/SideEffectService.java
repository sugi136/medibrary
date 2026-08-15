package com.medibrary.api.service;

import com.medibrary.api.adapter.EyakClient;
import com.medibrary.api.adapter.ExternalLookupResult;
import com.medibrary.api.adapter.OpenFdaClient;
import com.medibrary.api.dto.DrugDtos;
import com.medibrary.api.entity.Drug;
import com.medibrary.api.service.cache.SideEffectCacheService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Supplier;

@Service
public class SideEffectService {
    private static final String DOMESTIC = "domestic";
    private static final String OVERSEAS = "overseas";

    private final DrugService drugService;
    private final SideEffectCacheService cacheService;
    private final EyakClient eyakClient;
    private final OpenFdaClient openFdaClient;

    public SideEffectService(DrugService drugService,
                             SideEffectCacheService cacheService,
                             EyakClient eyakClient,
                             OpenFdaClient openFdaClient) {
        this.drugService = drugService;
        this.cacheService = cacheService;
        this.eyakClient = eyakClient;
        this.openFdaClient = openFdaClient;
    }

    public DrugDtos.SideEffectsResponse getSideEffects(String drugId, String source) {
        Drug drug = drugService.findDrug(drugId);
        DrugDtos.SideEffectResult domestic = isOverseasOnly(source)
                ? null : load(DOMESTIC, drug, () -> eyakClient.fetchDomesticSideEffects(drug));
        DrugDtos.SideEffectResult overseas = isDomesticOnly(source)
                ? null : load(OVERSEAS, drug, () -> openFdaClient.fetchOverseasSideEffects(drug));
        return new DrugDtos.SideEffectsResponse(domestic, overseas);
    }

    private DrugDtos.SideEffectResult load(String source,
                                           Drug drug,
                                           Supplier<ExternalLookupResult> externalSupplier) {
        return cacheService.findFresh(drug.getId(), source)
                .map(items -> new DrugDtos.SideEffectResult(true, items, null))
                .orElseGet(() -> fetchAndCache(source, drug, externalSupplier));
    }

    private DrugDtos.SideEffectResult fetchAndCache(String source,
                                                     Drug drug,
                                                     Supplier<ExternalLookupResult> externalSupplier) {
        ExternalLookupResult externalResult = externalSupplier.get();
        if (!externalResult.available()) {
            return new DrugDtos.SideEffectResult(false, List.of(), externalResult.message());
        }
        cacheService.save(drug.getId(), source, externalResult.items());
        return new DrugDtos.SideEffectResult(true, externalResult.items(), null);
    }

    private boolean isOverseasOnly(String source) {
        return OVERSEAS.equals(source);
    }

    private boolean isDomesticOnly(String source) {
        return DOMESTIC.equals(source);
    }
}
