package com.medibrary.api.service.cache;

import com.medibrary.api.adapter.ExternalContraindicationResult;
import com.medibrary.api.dto.DrugDtos;
import com.medibrary.api.dto.FavoriteDtos;
import com.medibrary.api.entity.Drug;
import com.medibrary.api.entity.DurPair;
import com.medibrary.api.repository.DrugRepository;
import com.medibrary.api.repository.DurPairRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ContraindicationCacheService {
    private final DurPairRepository durPairRepository;
    private final DrugRepository drugRepository;

    public ContraindicationCacheService(DurPairRepository durPairRepository, DrugRepository drugRepository) {
        this.durPairRepository = durPairRepository;
        this.drugRepository = drugRepository;
    }

    @Transactional(readOnly = true)
    public List<DrugDtos.ContraindicationItem> findFreshForDrug(String drugId, LocalDateTime freshnessThreshold) {
        Map<String, DrugDtos.ContraindicationItem> uniqueItems = new LinkedHashMap<>();
        for (DurPair pair : durPairRepository.findAllInvolvingDrug(drugId)) {
            if (pair.getFetchedAt() == null || pair.getFetchedAt().isBefore(freshnessThreshold)) {
                continue;
            }
            Drug counterpart = pair.getDrugA().getId().equals(drugId) ? pair.getDrugB() : pair.getDrugA();
            uniqueItems.putIfAbsent(counterpart.getId(), toItem(counterpart, pair));
        }
        return List.copyOf(uniqueItems.values());
    }

    @Transactional
    public List<DrugDtos.ContraindicationItem> cacheAndFind(Drug sourceDrug,
                                                             List<ExternalContraindicationResult.Item> externalItems) {
        Map<String, DrugDtos.ContraindicationItem> uniqueItems = new LinkedHashMap<>();
        for (ExternalContraindicationResult.Item item : externalItems) {
            if (!isCacheableCounterpart(sourceDrug, item)) {
                String responseKey = item.drugId() == null || item.drugId().isBlank() ? item.name() : item.drugId();
                uniqueItems.putIfAbsent(responseKey, new DrugDtos.ContraindicationItem(
                        item.drugId(), item.name(), null, item.type(), item.reason()));
                continue;
            }
            Drug counterpart = drugRepository.findById(item.drugId())
                    .orElseGet(() -> drugRepository.save(new Drug(item.drugId(), item.name().trim())));
            DurPair pair = upsertNormalizedPair(sourceDrug, counterpart, item.reason(), item.type());
            uniqueItems.putIfAbsent(counterpart.getId(), toItem(counterpart, pair));
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

    private DurPair upsertNormalizedPair(Drug first, Drug second, String reason, String severity) {
        Drug drugA = first.getId().compareTo(second.getId()) < 0 ? first : second;
        Drug drugB = drugA == first ? second : first;
        return durPairRepository.findByDrugAIdAndDrugBId(drugA.getId(), drugB.getId())
                .map(existing -> updatePair(existing, reason, severity))
                .orElseGet(() -> durPairRepository.save(new DurPair(drugA, drugB, reason, severity)));
    }

    private DurPair updatePair(DurPair pair, String reason, String severity) {
        pair.setReason(reason);
        pair.setSeverity(severity);
        return pair;
    }

    private DrugDtos.ContraindicationItem toItem(Drug counterpart, DurPair pair) {
        return new DrugDtos.ContraindicationItem(
                counterpart.getId(), counterpart.getName(), counterpart.getManufacturer(),
                pair.getSeverity(), pair.getReason());
    }

    private boolean isCacheableCounterpart(Drug sourceDrug, ExternalContraindicationResult.Item item) {
        return item.drugId() != null && !item.drugId().isBlank()
                && item.name() != null && !item.name().isBlank()
                && !sourceDrug.getId().equals(item.drugId());
    }
}
