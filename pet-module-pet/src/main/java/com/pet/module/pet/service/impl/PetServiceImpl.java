package com.pet.module.pet.service.impl;

import com.github.pagehelper.PageHelper;
import com.pet.common.enums.ResultCodeEnum;
import com.pet.common.exception.BusinessException;
import com.pet.module.pet.mapper.PetCategoryMapper;
import com.pet.module.pet.mapper.PetFavoriteMapper;
import com.pet.module.pet.mapper.PetImageMapper;
import com.pet.module.pet.mapper.PetInfoMapper;
import com.pet.module.pet.model.dto.PetPublishDto;
import com.pet.module.pet.model.dto.PetUpdateDto;
import com.pet.module.pet.model.entity.PetCategory;
import com.pet.module.pet.model.entity.PetFavorite;
import com.pet.module.pet.model.entity.PetImage;
import com.pet.module.pet.model.entity.PetInfo;
import com.pet.module.pet.model.vo.PetDetailVo;
import com.pet.module.pet.model.vo.PetListVo;
import com.pet.module.pet.model.vo.PetSelectVo;
import com.pet.module.pet.service.PetImageService;
import com.pet.module.pet.service.PetService;
import com.pet.module.system.mapper.UserMapper;
import com.pet.module.system.model.entity.SysUser;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@CacheConfig(cacheNames = "pet")
public class PetServiceImpl implements PetService {

    @Autowired
    private PetInfoMapper petInfoMapper;

    @Autowired
    private PetCategoryMapper petCategoryMapper;

    @Autowired
    private PetImageMapper petImageMapper;

    @Autowired
    private PetFavoriteMapper petFavoriteMapper;

    @Autowired
    private PetImageService petImageService;

    @Autowired
    private UserMapper userMapper;

    @Cacheable(key = "'list:' + #categoryId + ':' + #keyword + ':' + #status")
    @Override
    public List<PetListVo> getPetList(Long categoryId, String keyword, String status, int page, int size) {
        PageHelper.startPage(page, size);
        // status 为 null 时不筛选（管理员可见全部），由调用方自行传入
        String queryStatus = (status != null && !status.isEmpty()) ? status : null;
        List<PetInfo> list = petInfoMapper.selectPage(categoryId, keyword, queryStatus);
        return list.stream().map(this::convertToListVo).collect(Collectors.toList());
    }

    @Cacheable(key = "'detail:' + #petId + ':' + (#userId != null ? #userId : 0)")
    @Override
    public PetDetailVo getPetDetail(Long petId, Long userId) {
        PetInfo pet = petInfoMapper.selectById(petId);
        if (pet == null) {
            throw new BusinessException(ResultCodeEnum.PET_NOT_FOUND);
        }
        return convertToDetailVo(pet, userId);
    }

    @CacheEvict(allEntries = true)
    @Override
    @Transactional
    public Long publish(Long userId, PetPublishDto dto) {
        if (dto.getImages() == null || dto.getImages().size() < 3) {
            throw new BusinessException(ResultCodeEnum.PET_IMAGE_MIN);
        }

        PetCategory category = petCategoryMapper.selectById(dto.getCategoryId());
        if (category == null) {
            throw new BusinessException(ResultCodeEnum.CATEGORY_NOT_FOUND);
        }

        PetInfo pet = new PetInfo();
        pet.setUserId(userId);
        pet.setCategoryId(dto.getCategoryId());
        pet.setName(dto.getName());
        pet.setAge(dto.getAge());
        pet.setGender(dto.getGender());
        pet.setIsNeutered(dto.getIsNeutered());
        pet.setIsVaccinated(dto.getIsVaccinated());
        pet.setHealthCert(dto.getHealthCert());
        pet.setPersonality(dto.getPersonality());
        pet.setHabit(dto.getHabit());
        pet.setReason(dto.getReason());
        pet.setStatus("PENDING");
        petInfoMapper.insert(pet);

        // 保存图片
        petImageService.saveImages(pet.getId(), dto.getImages());

        return pet.getId();
    }

    @CacheEvict(allEntries = true)
    @Override
    @Transactional
    public void update(Long userId, Long petId, PetUpdateDto dto) {
        PetInfo pet = petInfoMapper.selectById(petId);
        if (pet == null) {
            throw new BusinessException(ResultCodeEnum.PET_NOT_FOUND);
        }
        if (!pet.getUserId().equals(userId)) {
            throw new BusinessException(ResultCodeEnum.ROLE_REQUIRED, "无权修改该宠物信息");
        }

        PetInfo update = new PetInfo();
        update.setId(petId);
        update.setCategoryId(dto.getCategoryId());
        update.setName(dto.getName());
        update.setAge(dto.getAge());
        update.setGender(dto.getGender());
        update.setIsNeutered(dto.getIsNeutered());
        update.setIsVaccinated(dto.getIsVaccinated());
        update.setHealthCert(dto.getHealthCert());
        update.setPersonality(dto.getPersonality());
        update.setHabit(dto.getHabit());
        update.setReason(dto.getReason());
        // 修改后状态重置为待审核
        update.setStatus("PENDING");
        petInfoMapper.updateById(update);

        // 更新图片
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            petImageService.deleteByPetId(petId);
            petImageService.saveImages(petId, dto.getImages());
        }
    }

    @CacheEvict(allEntries = true)
    @Override
    public void offline(Long userId, Long petId) {
        PetInfo pet = petInfoMapper.selectById(petId);
        if (pet == null) {
            throw new BusinessException(ResultCodeEnum.PET_NOT_FOUND);
        }
        if (!pet.getUserId().equals(userId)) {
            throw new BusinessException(ResultCodeEnum.ROLE_REQUIRED, "无权下架该宠物");
        }
        PetInfo update = new PetInfo();
        update.setId(petId);
        update.setStatus("OFFLINE");
        petInfoMapper.updateById(update);
    }

    @Override
    public List<PetListVo> getUserPets(Long userId) {
        List<PetInfo> list = petInfoMapper.selectByUserId(userId);
        return list.stream().map(this::convertToListVo).collect(Collectors.toList());
    }

    @Override
    public List<PetListVo> getPendingPets() {
        List<PetInfo> list = petInfoMapper.selectPendingList();
        return list.stream().map(this::convertToListVo).collect(Collectors.toList());
    }

    @Override
    public List<PetListVo> getReviewedByUser(Long reviewerId) {
        List<PetInfo> list = petInfoMapper.selectReviewedByReviewer(reviewerId);
        return list.stream().map(this::convertToListVo).collect(Collectors.toList());
    }

    @CacheEvict(allEntries = true)
    @Override
    @Transactional
    public void adminUpdateStatus(Long petId, String status) {
        PetInfo pet = petInfoMapper.selectById(petId);
        if (pet == null) {
            throw new BusinessException(ResultCodeEnum.PET_NOT_FOUND);
        }
        PetInfo update = new PetInfo();
        update.setId(petId);
        update.setStatus(status);
        petInfoMapper.updateById(update);
    }

    @Override
    public List<PetSelectVo> getSelectablePets() {
        List<String> statuses = Arrays.asList("PENDING", "FIRST_PASS", "APPROVED", "ADOPTED");
        List<PetInfo> list = petInfoMapper.selectByStatusList(statuses);
        return list.stream().map(p -> {
            PetSelectVo vo = new PetSelectVo();
            vo.setId(p.getId());
            vo.setName(p.getName());
            vo.setStatus(p.getStatus());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public PetDetailVo getPetDetailForAdmin(Long petId) {
        return getPetDetail(petId, null);
    }

    // ========== 私有转换方法 ==========

    private PetListVo convertToListVo(PetInfo pet) {
        PetListVo vo = new PetListVo();
        BeanUtils.copyProperties(pet, vo);

        // 分类名称
        PetCategory category = petCategoryMapper.selectById(pet.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getName());
        }

        // 封面图（第1张）
        List<PetImage> images = petImageMapper.selectByPetId(pet.getId());
        if (!images.isEmpty()) {
            vo.setCoverImage(images.get(0).getImageUrl());
        }

        // 送养人信息
        SysUser user = userMapper.selectById(pet.getUserId());
        if (user != null) {
            vo.setUserNickname(user.getNickname());
            vo.setUserAvatar(user.getAvatar());
        }

        return vo;
    }

    private PetDetailVo convertToDetailVo(PetInfo pet, Long userId) {
        PetDetailVo vo = new PetDetailVo();
        BeanUtils.copyProperties(pet, vo);

        // 分类名称
        PetCategory category = petCategoryMapper.selectById(pet.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getName());
        }

        // 图片列表
        List<PetImage> images = petImageMapper.selectByPetId(pet.getId());
        vo.setImages(images.stream().map(PetImage::getImageUrl).collect(Collectors.toList()));

        // 收藏数
        vo.setFavoriteCount(petFavoriteMapper.countByPetId(pet.getId()));

        // 当前用户是否已收藏（仅当登录时查询）
        if (userId != null) {
            PetFavorite existing = petFavoriteMapper.selectByUserAndPet(userId, pet.getId());
            vo.setIsFavorited(existing != null);
        } else {
            vo.setIsFavorited(false);
        }

        // 送养人信息
        SysUser user = userMapper.selectById(pet.getUserId());
        if (user != null) {
            vo.setUserNickname(user.getNickname());
            vo.setUserAvatar(user.getAvatar());
            vo.setUserPhone(user.getPhone());
        }

        return vo;
    }
}