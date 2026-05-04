package com.pet.module.pet.service.impl;

import com.pet.common.enums.ResultCodeEnum;
import com.pet.common.exception.BusinessException;
import com.pet.module.pet.mapper.PetFavoriteMapper;
import com.pet.module.pet.mapper.PetImageMapper;
import com.pet.module.pet.mapper.PetInfoMapper;
import com.pet.module.pet.model.entity.PetFavorite;
import com.pet.module.pet.model.entity.PetImage;
import com.pet.module.pet.model.entity.PetInfo;
import com.pet.module.pet.model.vo.PetListVo;
import com.pet.module.pet.service.PetFavoriteService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PetFavoriteServiceImpl implements PetFavoriteService {

    @Autowired
    private PetFavoriteMapper petFavoriteMapper;

    @Autowired
    private PetInfoMapper petInfoMapper;

    @Autowired
    private PetImageMapper petImageMapper;

    @Override
    @Transactional
    public void favorite(Long userId, Long petId) {
        PetInfo pet = petInfoMapper.selectById(petId);
        if (pet == null) {
            throw new BusinessException(ResultCodeEnum.PET_NOT_FOUND);
        }
        PetFavorite existing = petFavoriteMapper.selectByUserAndPet(userId, petId);
        if (existing != null) {
            return;
        }
        PetFavorite fav = new PetFavorite();
        fav.setUserId(userId);
        fav.setPetId(petId);
        petFavoriteMapper.insert(fav);
    }

    @Override
    @Transactional
    public void unfavorite(Long userId, Long petId) {
        petFavoriteMapper.deleteByUserAndPet(userId, petId);
    }

    @Override
    public List<PetListVo> getMyFavorites(Long userId) {
        List<PetFavorite> favs = petFavoriteMapper.selectByUserId(userId);
        return favs.stream().map(f -> {
            PetInfo pet = petInfoMapper.selectById(f.getPetId());
            if (pet == null) return null;
            PetListVo vo = new PetListVo();
            BeanUtils.copyProperties(pet, vo);
            List<PetImage> images = petImageMapper.selectByPetId(pet.getId());
            if (!images.isEmpty()) {
                vo.setCoverImage(images.get(0).getImageUrl());
            }
            return vo;
        }).filter(v -> v != null).collect(Collectors.toList());
    }
}