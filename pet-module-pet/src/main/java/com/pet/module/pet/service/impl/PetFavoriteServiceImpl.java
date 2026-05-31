package com.pet.module.pet.service.impl;

import com.pet.common.enums.ResultCodeEnum;
import com.pet.common.event.NotificationEvent;
import com.pet.common.exception.BusinessException;
import com.pet.module.pet.mapper.PetCategoryMapper;
import com.pet.module.pet.mapper.PetFavoriteMapper;
import com.pet.module.pet.mapper.PetImageMapper;
import com.pet.module.pet.mapper.PetInfoMapper;
import com.pet.module.pet.model.entity.PetCategory;
import com.pet.module.system.mapper.UserMapper;
import com.pet.module.system.model.entity.SysUser;
import com.pet.module.pet.model.entity.PetFavorite;
import com.pet.module.pet.model.entity.PetImage;
import com.pet.module.pet.model.entity.PetInfo;
import com.pet.module.pet.model.vo.PetListVo;
import com.pet.module.pet.service.PetFavoriteService;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
public class PetFavoriteServiceImpl implements PetFavoriteService {

    private final PetFavoriteMapper petFavoriteMapper;

    private final PetInfoMapper petInfoMapper;

    private final PetImageMapper petImageMapper;

    private final PetCategoryMapper petCategoryMapper;

    private final UserMapper userMapper;

    private final ApplicationEventPublisher eventPublisher;

    public PetFavoriteServiceImpl(
            PetFavoriteMapper petFavoriteMapper,
            PetInfoMapper petInfoMapper,
            PetImageMapper petImageMapper,
            PetCategoryMapper petCategoryMapper,
            UserMapper userMapper,
            ApplicationEventPublisher eventPublisher) {
        this.petFavoriteMapper = petFavoriteMapper;
        this.petInfoMapper = petInfoMapper;
        this.petImageMapper = petImageMapper;
        this.petCategoryMapper = petCategoryMapper;
        this.userMapper = userMapper;
        this.eventPublisher = eventPublisher;
    }


    @Override
    @CacheEvict(cacheNames = "pet", allEntries = true)
    @Transactional
    public void favorite(Long userId, Long petId, Long folderId) {
        PetInfo pet = petInfoMapper.selectById(petId);
        if (pet == null) {
            throw new BusinessException(ResultCodeEnum.PET_NOT_FOUND);
        }
        PetFavorite existing = petFavoriteMapper.selectByUserAndPet(userId, petId);
        if (existing != null) {
            // 更新收藏夹（支持移出：folderId=null）
            boolean needsUpdate = (folderId == null && existing.getFolderId() != null)
                    || (folderId != null && !folderId.equals(existing.getFolderId()));
            if (needsUpdate) {
                existing.setFolderId(folderId);
                petFavoriteMapper.updateFolderId(existing);
            }
            return;
        }
        PetFavorite fav = new PetFavorite();
        fav.setUserId(userId);
        fav.setPetId(petId);
        fav.setFolderId(folderId);
        petFavoriteMapper.insert(fav);

        // 通知送养人：有人收藏了你的宠物
        if (!pet.getUserId().equals(userId)) {
            SysUser favoriter = userMapper.selectById(userId);
            String name = favoriter != null ? (favoriter.getNickname() != null ? favoriter.getNickname() : favoriter.getUsername()) : "用户";
            String petName = pet.getName() != null ? pet.getName() : "宠物";
            eventPublisher.publishEvent(new NotificationEvent(
                    pet.getUserId(), "PET_FAVORITED",
                    "宠物被收藏",
                    name + "收藏了你发布的" + petName,
                    petId));
        }
    }

    @Override
    @CacheEvict(cacheNames = "pet", allEntries = true)
    @Transactional
    public void unfavorite(Long userId, Long petId) {
        petFavoriteMapper.deleteByUserAndPet(userId, petId);
    }

    @Override
    public List<PetListVo> getMyFavoritesByFolder(Long userId, Long folderId) {
        List<PetFavorite> favs = petFavoriteMapper.selectByUserIdAndFolder(userId, folderId);
        return buildFavList(favs);
    }

    @Override
    public Map<Long, Integer> getFavoriteCounts(Long userId) {
        List<PetFavorite> favs = petFavoriteMapper.selectByUserId(userId);
        Map<Long, Integer> counts = new HashMap<>();
        for (PetFavorite f : favs) {
            Long fid = f.getFolderId();
            if (fid != null) {
                counts.put(fid, counts.getOrDefault(fid, 0) + 1);
            }
        }
        return counts;
    }

    @Override
    public Map<String, Object> getFavoritesWithCounts(Long userId) {
        List<PetFavorite> favs = petFavoriteMapper.selectByUserId(userId);
        List<PetListVo> list = buildFavList(favs);
        Map<Long, Integer> counts = new HashMap<>();
        for (PetFavorite f : favs) {
            Long fid = f.getFolderId();
            if (fid != null) {
                counts.put(fid, counts.getOrDefault(fid, 0) + 1);
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("counts", counts);
        return result;
    }

    @Override
    public List<PetListVo> getMyFavorites(Long userId) {
        List<PetFavorite> favs = petFavoriteMapper.selectByUserId(userId);
        return buildFavList(favs);
    }

    /**
     * 将 PetFavorite 列表转为 PetListVo（含封面、分类、送养人信息）
     */
    private List<PetListVo> buildFavList(List<PetFavorite> favs) {
        return favs.stream().map(f -> {
            PetInfo pet = petInfoMapper.selectById(f.getPetId());
            if (pet == null) return null;
            PetListVo vo = new PetListVo();
            BeanUtils.copyProperties(pet, vo);
            // 封面图
            List<PetImage> images = petImageMapper.selectByPetId(pet.getId());
            if (!images.isEmpty()) {
                vo.setCoverImage(images.get(0).getImageUrl());
            }
            // 分类名称
            PetCategory category = petCategoryMapper.selectById(pet.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }
            // 送养人信息
            SysUser user = userMapper.selectById(pet.getUserId());
            if (user != null) {
                vo.setUserNickname(user.getNickname());
                vo.setUserAvatar(user.getAvatar());
            }
            return vo;
        }).filter(v -> v != null).collect(Collectors.toList());
    }
}