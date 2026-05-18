package com.pet.module.pet.service.impl;

import com.github.pagehelper.PageHelper;
import com.pet.common.enums.ResultCodeEnum;
import com.pet.common.event.NotificationEvent;
import com.pet.common.exception.BusinessException;
import com.pet.module.pet.mapper.PetCategoryMapper;
import com.pet.module.pet.mapper.PetFavoriteMapper;
import com.pet.module.pet.mapper.PetImageMapper;
import com.pet.module.pet.mapper.PetInfoMapper;
import com.pet.module.pet.mapper.PetReviewRecordMapper;
import com.pet.module.pet.model.dto.PetPublishDto;
import com.pet.module.pet.model.dto.PetUpdateDto;
import com.pet.module.pet.model.entity.PetCategory;
import com.pet.module.pet.model.entity.PetFavorite;
import com.pet.module.pet.model.entity.PetImage;
import com.pet.module.pet.model.entity.PetInfo;
import com.pet.module.pet.model.vo.PetDetailVo;
import com.pet.module.pet.model.vo.PetListVo;
import com.pet.module.pet.model.vo.PetSelectVo;
import com.pet.module.pet.model.vo.PetTimelineEvent;
import com.pet.module.pet.model.vo.ReviewHistoryVo;
import com.pet.module.pet.model.entity.PetReviewRecord;
import com.pet.module.pet.service.PetImageService;
import com.pet.module.pet.service.PetService;
import com.pet.module.system.mapper.UserMapper;
import com.pet.module.system.mapper.UserRoleMapper;
import com.pet.module.system.model.entity.SysUser;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
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
    private PetReviewRecordMapper petReviewRecordMapper;

    @Autowired
    private PetImageService petImageService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private UserRoleMapper userRoleMapper;

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

        // 通知所有志愿者（排除自己）：有新宠物待初审
        SysUser donor = userMapper.selectById(userId);
        String donorName = donor != null ? (donor.getNickname() != null ? donor.getNickname() : donor.getUsername()) : "送养人";
        String petName = pet.getName() != null ? pet.getName() : "宠物";
        List<Long> volunteerIds = userRoleMapper.selectUserIdsByRoleCode("VOLUNTEER");
        for (Long vid : volunteerIds) {
            if (vid.equals(userId)) continue; // 跳过自己
            eventPublisher.publishEvent(new NotificationEvent(
                    vid, "PET_PUBLISH",
                    "新宠物待审核",
                    donorName + "发布了" + petName + "，请尽快前往审核",
                    pet.getId()));
        }

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
        String oldStatus = pet.getStatus();
        update.setStatus("PENDING");
        petInfoMapper.updateById(update);

        // 更新图片
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            petImageService.deleteByPetId(petId);
            petImageService.saveImages(petId, dto.getImages());
        }

        // 状态从非PENDING变回PENDING → 通知志愿者有新的待审核宠物
        if (!"PENDING".equals(oldStatus)) {
            SysUser donor = userMapper.selectById(userId);
            String donorName = donor != null ? (donor.getNickname() != null ? donor.getNickname() : donor.getUsername()) : "送养人";
            String petName = pet.getName() != null ? pet.getName() : "宠物";
            String action = "REJECTED".equals(oldStatus) ? "重新提交了" : "更新了";
            List<Long> volunteerIds = userRoleMapper.selectUserIdsByRoleCode("VOLUNTEER");
            for (Long vid : volunteerIds) {
                if (vid.equals(userId)) continue; // 跳过自己
                eventPublisher.publishEvent(new NotificationEvent(
                        vid, "PET_PUBLISH",
                        "新宠物待审核",
                        donorName + action + petName + "，请尽快前往审核",
                        petId));
            }
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

    @CacheEvict(allEntries = true)
    @Override
    @Transactional
    public void withdraw(Long userId, Long petId) {
        PetInfo pet = petInfoMapper.selectById(petId);
        if (pet == null) {
            throw new BusinessException(ResultCodeEnum.PET_NOT_FOUND);
        }
        if (!pet.getUserId().equals(userId)) {
            throw new BusinessException(ResultCodeEnum.ROLE_REQUIRED, "无权撤回该宠物");
        }

        String currentStatus = pet.getStatus();
        String newStatus;
        String errorMsg = null;

        if ("PENDING".equals(currentStatus)) {
            // 待审核 → 下架，直接用乐观锁防并发，不查旧审核记录
            newStatus = "OFFLINE";
        } else if ("REJECTED".equals(currentStatus)) {
            // 已打回 → 重新提交审核（打回后再编辑走的 update，不会走这里）
            newStatus = "PENDING";
        } else if ("FIRST_PASS".equals(currentStatus)) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST,
                    "该宠物已被志愿者审核，无法撤回，请联系管理员处理");
        } else {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST,
                    "该状态不支持撤回");
        }

        // 乐观锁：仅当状态还是 currentStatus 时更新，防止并发审核冲突
        int rows = petInfoMapper.updateStatusIfPending(petId, newStatus, currentStatus, null);
        if (rows == 0) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST,
                    "宠物状态已变更，请刷新后重试");
        }
    }

    @Override
    public List<PetListVo> getUserPets(Long userId) {
        List<PetInfo> list = petInfoMapper.selectByUserId(userId);
        return list.stream().map(this::convertToListVo).collect(Collectors.toList());
    }

    @Override
    public List<PetListVo> getPendingPets() {
        return getPendingPets(null);
    }

    @Override
    public List<PetListVo> getPendingPets(Long excludeUserId) {
        List<PetInfo> list = petInfoMapper.selectPendingList();
        return list.stream()
                .filter(p -> excludeUserId == null || !excludeUserId.equals(p.getUserId()))
                .map(this::convertToListVo)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReviewHistoryVo> getReviewedByUser(Long reviewerId) {
        return petReviewRecordMapper.selectReviewHistoryByReviewer(reviewerId);
    }

    @Override
    public List<PetTimelineEvent> getPetTimeline(Long petId) {
        List<PetTimelineEvent> events = new java.util.ArrayList<>();

        PetInfo pet = petInfoMapper.selectById(petId);
        if (pet == null) return events;

        // 1. 发布事件
        PetTimelineEvent publish = new PetTimelineEvent();
        publish.setType("PUBLISH");
        publish.setCreatedAt(pet.getCreatedAt());
        events.add(publish);

        // 2. 审核记录
        List<PetReviewRecord> reviews = petReviewRecordMapper.selectByPetId(petId);
        for (PetReviewRecord r : reviews) {
            PetTimelineEvent e = new PetTimelineEvent();
            e.setType("FIRST".equals(r.getReviewType()) ? "FIRST_REVIEW" : "FINAL_REVIEW");
            e.setAction(r.getAction());
            e.setRemark(r.getRemark());
            e.setReviewerType("FIRST".equals(r.getReviewType()) ? "志愿者" : "管理员");
            e.setCreatedAt(r.getCreatedAt());
            events.add(e);
        }

        // 3. 按时间升序排序
        events.sort((a, b) -> {
            if (a.getCreatedAt() == null) return -1;
            if (b.getCreatedAt() == null) return 1;
            return a.getCreatedAt().compareTo(b.getCreatedAt());
        });

        // 4. 扫描模式，自动补全中间事件
        List<PetTimelineEvent> withEdits = new java.util.ArrayList<>();
        String lastType = null;
        boolean lastWasRejected = false;

        for (int i = 0; i < events.size(); i++) {
            PetTimelineEvent evt = events.get(i);
            String type = evt.getType();
            String action = evt.getAction();

            // === 检测编辑重新提交 ===
            // 场景1：打回（初审/终审）→ 后面出现新的初审 → 中间有编辑
            if ("FIRST_REVIEW".equals(type) && lastWasRejected) {
                withEdits.add(createEditEvent(evt.getCreatedAt()));
            }
            // 场景2：终审通过 → 后面出现新的初审 → 中间有编辑（被下架后重新上架等）
            if ("FIRST_REVIEW".equals(type) && "FINAL_REVIEW".equals(lastType) && "APPROVED".equals(action)) {
                withEdits.add(createEditEvent(evt.getCreatedAt()));
            }

            withEdits.add(evt);

            // 更新状态跟踪
            lastType = type;
            if ("FIRST_REVIEW".equals(type) || "FINAL_REVIEW".equals(type)) {
                lastWasRejected = "REJECTED".equals(action);
            }
        }

        // 5. 检测当前状态是否有完结后的事件（在最后追加）
        String currentStatus = pet.getStatus();
        java.time.LocalDateTime petUpdated = pet.getUpdatedAt();
        PetTimelineEvent lastEvent = withEdits.isEmpty() ? null : withEdits.get(withEdits.size() - 1);
        java.time.LocalDateTime lastTime = lastEvent != null ? lastEvent.getCreatedAt() : null;

        if ("OFFLINE".equals(currentStatus) && petUpdated != null
                && (lastTime == null || !petUpdated.isBefore(lastTime))) {
            PetTimelineEvent offline = new PetTimelineEvent();
            offline.setType("OFFLINE");
            offline.setCreatedAt(petUpdated);
            withEdits.add(offline);
        } else if ("PENDING".equals(currentStatus) && petUpdated != null
                && lastTime != null && petUpdated.isAfter(lastTime)) {
            String lastEvtType = lastEvent != null ? lastEvent.getType() : "";
            if (!"EDIT".equals(lastEvtType) && !"PUBLISH".equals(lastEvtType)) {
                withEdits.add(createEditEvent(petUpdated));
            }
        }

        // 6. 最终按时间倒序输出
        withEdits.sort((a, b) -> {
            if (a.getCreatedAt() == null) return 1;
            if (b.getCreatedAt() == null) return -1;
            return b.getCreatedAt().compareTo(a.getCreatedAt());
        });

        return withEdits;
    }

    private PetTimelineEvent createEditEvent(java.time.LocalDateTime time) {
        PetTimelineEvent e = new PetTimelineEvent();
        e.setType("EDIT");
        e.setCreatedAt(time);
        return e;
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
        List<String> statuses = Arrays.asList("ADOPTED");
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