package com.medibrary.api.service;

import com.medibrary.api.entity.SearchHistory;
import com.medibrary.api.repository.SearchHistoryRepository;
import com.medibrary.api.repository.UserRepository;
import com.medibrary.api.service.search.DrugSearchCriteria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class SearchHistoryService {
    private final SearchHistoryRepository searchHistoryRepository;
    private final UserRepository userRepository;

    public SearchHistoryService(SearchHistoryRepository searchHistoryRepository, UserRepository userRepository) {
        this.searchHistoryRepository = searchHistoryRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void record(Long userId, DrugSearchCriteria criteria) {
        String queryText = toDisplayText(criteria);
        if (queryText.isBlank()) {
            return;
        }
        userRepository.findById(userId).ifPresent(user ->
                searchHistoryRepository.save(new SearchHistory(user, queryText))
        );
    }

    @Transactional(readOnly = true)
    public List<String> findRecentQueries(Long userId) {
        return searchHistoryRepository.findTop5ByUserIdOrderBySearchedAtDesc(userId).stream()
                .map(SearchHistory::getQueryText)
                .toList();
    }

    private String toDisplayText(DrugSearchCriteria criteria) {
        List<String> parts = new ArrayList<>();
        appendCondition(parts, "이름", criteria.normalizedName());
        appendCondition(parts, "모양", criteria.normalizedShape());
        appendCondition(parts, "색상", criteria.normalizedColor());
        return String.join(" · ", parts);
    }

    private void appendCondition(List<String> parts, String label, String value) {
        if (value != null && !value.isBlank()) {
            parts.add(label + ": " + value);
        }
    }
}
