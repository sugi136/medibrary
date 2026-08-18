package com.medibrary.api.service;

import com.medibrary.api.dto.FavoriteDtos;
import com.medibrary.api.entity.Drug;
import com.medibrary.api.entity.Favorite;
import com.medibrary.api.entity.User;
import com.medibrary.api.repository.FavoriteRepository;
import com.medibrary.api.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class FavoriteService {
    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final DrugService drugService;
    private final DurService durService;

    public FavoriteService(FavoriteRepository favoriteRepository, UserRepository userRepository,
                           DrugService drugService, DurService durService) {
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
        this.drugService = drugService;
        this.durService = durService;
    }

    @Transactional
    public void create(Long userId, FavoriteDtos.CreateFavoriteRequest request) {
        if (favoriteRepository.existsByUserIdAndDrugId(userId, request.drugId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 즐겨찾기에 등록된 약입니다.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 정보가 유효하지 않습니다."));
        Drug drug = drugService.findDrug(request.drugId());
        favoriteRepository.save(new Favorite(user, drug));
    }

    @Transactional(readOnly = true)
    public FavoriteDtos.FavoritesResponse getAll(Long userId) {
        List<Favorite> favorites = favoriteRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
        List<FavoriteDtos.FavoriteDrugSummary> summaries = favorites.stream()
                .map(FavoriteMapper::toSummary)
                .toList();
        List<String> drugIds = favorites.stream().map(favorite -> favorite.getDrug().getId()).toList();
        return new FavoriteDtos.FavoritesResponse(summaries, durService.checkFavoritePairs(drugIds));
    }

    @Transactional
    public void delete(Long userId, Long favoriteId) {
        Favorite favorite = favoriteRepository.findByIdAndUserId(favoriteId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "즐겨찾기 정보가 없습니다."));
        favoriteRepository.delete(favorite);
    }

    @Transactional
    public void deleteByDrugId(Long userId, String drugId) {
        Favorite favorite = favoriteRepository.findByUserIdAndDrugId(userId, drugId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "즐겨찾기 정보가 없습니다."));
        favoriteRepository.delete(favorite);
    }
}
