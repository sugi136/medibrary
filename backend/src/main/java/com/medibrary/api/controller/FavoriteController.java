package com.medibrary.api.controller;

import com.medibrary.api.dto.FavoriteDtos;
import com.medibrary.api.service.FavoriteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/favorites")
public class FavoriteController {
    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @GetMapping
    public FavoriteDtos.FavoritesResponse getAll(@AuthenticationPrincipal Long userId) {
        return favoriteService.getAll(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@AuthenticationPrincipal Long userId,
                       @Valid @RequestBody FavoriteDtos.CreateFavoriteRequest request) {
        favoriteService.create(userId, request);
    }

    @DeleteMapping("/drug/{drugId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteByDrugId(@AuthenticationPrincipal Long userId, @PathVariable String drugId) {
        favoriteService.deleteByDrugId(userId, drugId);
    }

    @DeleteMapping("/{favoriteId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Long userId, @PathVariable Long favoriteId) {
        favoriteService.delete(userId, favoriteId);
    }
}
