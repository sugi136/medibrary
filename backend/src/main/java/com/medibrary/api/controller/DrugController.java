package com.medibrary.api.controller;

import com.medibrary.api.dto.DrugDtos;
import com.medibrary.api.service.DrugService;
import com.medibrary.api.service.DuplicateWarningService;
import com.medibrary.api.service.DurService;
import com.medibrary.api.service.SideEffectService;
import com.medibrary.api.service.OverseasProductService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/drugs")
public class DrugController {
    private final DrugService drugService;
    private final SideEffectService sideEffectService;
    private final DurService durService;
    private final DuplicateWarningService duplicateWarningService;
    private final OverseasProductService overseasProductService;

    public DrugController(DrugService drugService,
                          SideEffectService sideEffectService,
                          DurService durService,
                          DuplicateWarningService duplicateWarningService,
                          OverseasProductService overseasProductService) {
        this.drugService = drugService;
        this.sideEffectService = sideEffectService;
        this.durService = durService;
        this.duplicateWarningService = duplicateWarningService;
        this.overseasProductService = overseasProductService;
    }

    @GetMapping("/search")
    public DrugDtos.SearchResponse search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String shape,
            @RequestParam(required = false) String color
    ) {
        return drugService.search(name, shape, color);
    }

    @GetMapping("/{drugId}")
    public DrugDtos.DrugDetail getDetail(@PathVariable String drugId) {
        return drugService.getDetail(drugId);
    }

    @GetMapping("/{drugId}/side-effects")
    public DrugDtos.SideEffectsResponse getSideEffects(
            @PathVariable String drugId,
            @RequestParam(defaultValue = "all") String source
    ) {
        return sideEffectService.getSideEffects(drugId, source);
    }

    @GetMapping("/{drugId}/duplicates")
    public DrugDtos.DuplicateWarningResponse getDuplicateWarnings(@PathVariable String drugId) {
        return duplicateWarningService.getWarnings(drugId);
    }

    @GetMapping("/{drugId}/contraindications")
    public DrugDtos.ContraindicationResponse getContraindications(@PathVariable String drugId) {
        return durService.getContraindications(drugId);
    }

    @GetMapping("/{drugId}/overseas-products")
    public DrugDtos.OverseasProductResponse getOverseasProducts(@PathVariable String drugId) {
        return overseasProductService.getOverseasProducts(drugId);
    }
}
