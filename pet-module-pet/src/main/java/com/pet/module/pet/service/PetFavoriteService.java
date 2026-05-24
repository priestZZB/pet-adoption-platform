package com.pet.module.pet.service;

import com.pet.module.pet.model.vo.PetListVo;
import java.util.List;

public interface PetFavoriteService {

    void favorite(Long userId, Long petId, Long folderId);

    void unfavorite(Long userId, Long petId);

    List<PetListVo> getMyFavorites(Long userId);

    List<PetListVo> getMyFavoritesByFolder(Long userId, Long folderId);

    java.util.Map<Long, Integer> getFavoriteCounts(Long userId);

    /** 一次性返回收藏列表 + 各收藏夹计数 */
    java.util.Map<String, Object> getFavoritesWithCounts(Long userId);
}