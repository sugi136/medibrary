package com.medibrary.api.service;

import com.medibrary.api.dto.FavoriteDtos;
import com.medibrary.api.repository.FavoriteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {
    private final FavoriteRepository favoriteRepository;
    private final DurService durService;
    private final SearchHistoryService searchHistoryService;

    public DashboardService(FavoriteRepository favoriteRepository,
                            DurService durService,
                            SearchHistoryService searchHistoryService) {
        this.favoriteRepository = favoriteRepository;
        this.durService = durService;
        this.searchHistoryService = searchHistoryService;
    }

    @Transactional(readOnly = true)
    public FavoriteDtos.DashboardSummary getSummary(Long userId) {
        var favorites = favoriteRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
        var drugIds = favorites.stream().map(favorite -> favorite.getDrug().getId()).toList();
        boolean hasDurWarning = !durService.checkFavoritePairs(drugIds).isEmpty();
        return new FavoriteDtos.DashboardSummary(
                searchHistoryService.findRecentQueries(userId), favorites.size(), hasDurWarning);
    }
}
