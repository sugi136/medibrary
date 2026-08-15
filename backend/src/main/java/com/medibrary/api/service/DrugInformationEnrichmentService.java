package com.medibrary.api.service;

import com.medibrary.api.adapter.ExternalDrugInformation;
import com.medibrary.api.adapter.EyakClient;
import com.medibrary.api.entity.Drug;
import org.springframework.stereotype.Service;

@Service
public class DrugInformationEnrichmentService {
    private final EyakClient eyakClient;

    public DrugInformationEnrichmentService(EyakClient eyakClient) {
        this.eyakClient = eyakClient;
    }

    public void enrichMissingFields(Drug drug) {
        if (hasAllDetailFields(drug)) {
            return;
        }

        ExternalDrugInformation information = eyakClient.fetchDrugInformation(drug);
        if (!information.available()) {
            return;
        }
        setIfBlank(drug::getEfficacy, drug::setEfficacy, information.efficacy());
        setIfBlank(drug::getUsageInfo, drug::setUsageInfo, information.usageInfo());
        setIfBlank(drug::getCaution, drug::setCaution, information.caution());
    }

    private boolean hasAllDetailFields(Drug drug) {
        return hasText(drug.getEfficacy()) && hasText(drug.getUsageInfo()) && hasText(drug.getCaution());
    }

    private void setIfBlank(java.util.function.Supplier<String> currentValue,
                            java.util.function.Consumer<String> setter,
                            String newValue) {
        if (!hasText(currentValue.get()) && hasText(newValue)) {
            setter.accept(newValue);
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
