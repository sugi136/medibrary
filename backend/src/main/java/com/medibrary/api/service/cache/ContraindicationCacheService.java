package com.medibrary.api.service.cache;

import com.medibrary.api.dto.DrugDtos;
import com.medibrary.api.dto.FavoriteDtos;
import com.medibrary.api.entity.Drug;
import com.medibrary.api.entity.DurPair;
import com.medibrary.api.repository.DurPairRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ContraindicationCacheService {
    private final DurPairRepository durPairRepository;

    public ContraindicationCacheService(DurPairRepository durPairRepository) {
        this.durPairRepository = durPairRepository;
    }

    @Transactional(readOnly = true)
    public List<DrugDtos.ContraindicationItem> findForDrug(String drugId) {
        Map<String, DrugDtos.ContraindicationItem> uniqueItems = new LinkedHashMap<>();
        for (DurPair pair : durPairRepository.findAllInvolvingDrug(drugId)) {
            Drug counterpart = pair.getDrugA().getId().equals(drugId) ? pair.getDrugB() : pair.getDrugA();
            uniqueItems.putIfAbsent(counterpart.getId(), new DrugDtos.ContraindicationItem(
                    counterpart.getId(), counterpart.getName(), null, pair.getSeverity(), pair.getReason()));
        }
        return List.copyOf(uniqueItems.values());
    }

    @Transactional(readOnly = true)
    public List<FavoriteDtos.DurWarning> findWarningsWithin(List<String> drugIds) {
        if (drugIds.size() < 2) {
            return List.of();
        }
        return durPairRepository.findAllWithinDrugIds(drugIds).stream()
                .map(pair -> new FavoriteDtos.DurWarning(
                        pair.getDrugA().getId(), pair.getDrugB().getId(), pair.getReason(), pair.getSeverity()))
                .toList();
    }
}
