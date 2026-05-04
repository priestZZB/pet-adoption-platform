package com.pet.module.pet.service;

import com.pet.module.pet.model.vo.PetListVo;
import java.util.List;

public interface PetFavoriteService {

    void favorite(Long userId, Long petId);

    void unfavorite(Long userId, Long petId);

    List<PetListVo> getMyFavorites(Long userId);
}