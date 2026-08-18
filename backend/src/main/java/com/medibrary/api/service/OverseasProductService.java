package com.medibrary.api.service;

import com.medibrary.api.adapter.OverseasProductClient;
import com.medibrary.api.dto.DrugDtos;
import com.medibrary.api.entity.Drug;
import org.springframework.stereotype.Service;

/**
 * 상세 화면 '해외에서는 이런 약' 탭 - REQ-F-015
 *
 * 탭을 처음 눌렀을 때만 호출되는 lazy loading 대상이며,
 * 외부 호출은 DB 트랜잭션 밖에서 수행한다(기존 부작용 조회와 동일한 원칙).
 */
@Service
public class OverseasProductService {
    private final DrugService drugService;
    private final OverseasProductClient overseasProductClient;

    public OverseasProductService(DrugService drugService,
                                  OverseasProductClient overseasProductClient) {
        this.drugService = drugService;
        this.overseasProductClient = overseasProductClient;
    }

    public DrugDtos.OverseasProductResponse getOverseasProducts(String drugId) {
        Drug drug = drugService.findDrug(drugId);
        return overseasProductClient.fetch(drug);
    }
}
