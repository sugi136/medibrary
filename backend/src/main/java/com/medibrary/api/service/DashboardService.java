package com.medibrary.api.service;

import com.medibrary.api.dto.FavoriteDtos;
import com.medibrary.api.repository.FavoriteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DashboardService {
    private final FavoriteRepository favoriteRepository;
    private final DurService durService;

    public DashboardService(FavoriteRepository favoriteRepository, DurService durService) {
        this.favoriteRepository = favoriteRepository;
        this.durService = durService;
    }

    @Transactional(readOnly = true)
    public FavoriteDtos.DashboardSummary getSummary(Long userId) {
        var favorites = favoriteRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
        var drugIds = favorites.stream().map(favorite -> favorite.getDrug().getId()).toList();
        boolean hasDurWarning = !durService.checkFavoritePairs(drugIds).isEmpty();
        // 현 ERD에는 최근 검색어 저장 테이블이 없으므로 초기 골격에서는 빈 목록을 반환한다.
        return new FavoriteDtos.DashboardSummary(List.of(), favorites.size(), hasDurWarning);
    }
}
