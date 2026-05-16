package com.pet.module.pet.service.impl;

import com.pet.common.enums.ResultCodeEnum;
import com.pet.common.event.NotificationEvent;
import com.pet.common.exception.BusinessException;
import com.pet.module.pet.mapper.PetInfoMapper;
import com.pet.module.pet.mapper.PetReviewRecordMapper;
import com.pet.module.pet.model.dto.PetReviewDto;
import com.pet.module.pet.model.entity.PetInfo;
import com.pet.module.pet.model.entity.PetReviewRecord;
import com.pet.module.pet.service.PetReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PetReviewServiceImpl implements PetReviewService {

    @Autowired
    private PetInfoMapper petInfoMapper;

    @Autowired
    private PetReviewRecordMapper petReviewRecordMapper;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    @CacheEvict(cacheNames = "pet", allEntries = true)
    public void firstReview(Long reviewerId, Long petId, PetReviewDto dto) {
        PetInfo pet = petInfoMapper.selectById(petId);
        if (pet == null) {
            throw new BusinessException(ResultCodeEnum.PET_NOT_FOUND);
        }
        if (!"PENDING".equals(pet.getStatus())) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "该宠物当前状态不可初审");
        }

        // 更新宠物状态
        PetInfo update = new PetInfo();
        update.setId(petId);
        if ("APPROVED".equals(dto.getAction())) {
            update.setStatus("FIRST_PASS");  // 初审通过 → 待终审
        } else if ("REJECTED".equals(dto.getAction())) {
            update.setStatus("REJECTED");
            update.setReviewRemark(dto.getRemark());
        } else {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "审核操作无效");
        }
        petInfoMapper.updateById(update);

        // 记录审核记录
        PetReviewRecord record = new PetReviewRecord();
        record.setPetId(petId);
        record.setReviewerId(reviewerId);
        record.setReviewType("FIRST");
        record.setAction(dto.getAction());
        record.setRemark(dto.getRemark());
        petReviewRecordMapper.insert(record);

        // 通知送养人初审结果
        String petName = pet.getName() != null ? pet.getName() : "宠物";
        if ("APPROVED".equals(dto.getAction())) {
            eventPublisher.publishEvent(new NotificationEvent(
                    pet.getUserId(), "PET_REVIEW",
                    "宠物初审通过",
                    "你发布的" + petName + "已通过初审，等待管理员终审",
                    petId));
        } else if ("REJECTED".equals(dto.getAction())) {
            String reason = dto.getRemark() != null ? dto.getRemark() : "信息不符合要求";
            eventPublisher.publishEvent(new NotificationEvent(
                    pet.getUserId(), "PET_REVIEW",
                    "宠物初审未通过",
                    "你发布的" + petName + "未通过初审，原因：" + reason,
                    petId));
        }
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "pet", allEntries = true)
    public void finalReview(Long adminId, Long petId, PetReviewDto dto) {
        PetInfo pet = petInfoMapper.selectById(petId);
        if (pet == null) {
            throw new BusinessException(ResultCodeEnum.PET_NOT_FOUND);
        }
        if (!"FIRST_PASS".equals(pet.getStatus())) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "该宠物尚未通过初审或非待终审状态");
        }

        PetInfo update = new PetInfo();
        update.setId(petId);
        if ("APPROVED".equals(dto.getAction())) {
            update.setStatus("APPROVED");  // 终审通过 → 完全通过可展示
        } else if ("REJECTED".equals(dto.getAction())) {
            update.setStatus("REJECTED");
            update.setReviewRemark(dto.getRemark());
        } else {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "审核操作无效");
        }
        petInfoMapper.updateById(update);

        PetReviewRecord record = new PetReviewRecord();
        record.setPetId(petId);
        record.setReviewerId(adminId);
        record.setReviewType("FINAL");
        record.setAction(dto.getAction());
        record.setRemark(dto.getRemark());
        petReviewRecordMapper.insert(record);

        // 通知送养人终审结果
        String petName = pet.getName() != null ? pet.getName() : "宠物";
        if ("APPROVED".equals(dto.getAction())) {
            eventPublisher.publishEvent(new NotificationEvent(
                    pet.getUserId(), "PET_REVIEW",
                    "宠物终审通过",
                    "你发布的" + petName + "已通过终审，现在可以被申请领养了",
                    petId));
        } else if ("REJECTED".equals(dto.getAction())) {
            String reason = dto.getRemark() != null ? dto.getRemark() : "信息不符合要求";
            eventPublisher.publishEvent(new NotificationEvent(
                    pet.getUserId(), "PET_REVIEW",
                    "宠物终审未通过",
                    "你发布的" + petName + "未通过终审，原因：" + reason,
                    petId));
        }
    }
}