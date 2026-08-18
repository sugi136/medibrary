package com.medibrary.api.repository;

import com.medibrary.api.entity.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {
    List<SearchHistory> findTop5ByUserIdOrderBySearchedAtDesc(Long userId);
}
