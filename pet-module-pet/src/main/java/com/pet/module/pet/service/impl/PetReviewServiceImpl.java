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
import com.pet.module.system.mapper.UserRoleMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
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

    @Autowired
    private UserRoleMapper userRoleMapper;

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
        // 禁止审核自己发布的宠物
        if (pet.getUserId().equals(reviewerId)) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "不能审核自己发布的宠物");
        }

        // 使用乐观锁更新状态（仅当 status = 'PENDING' 时更新）
        String newStatus = "APPROVED".equals(dto.getAction()) ? "FIRST_PASS" : "REJECTED";
        String remark = "REJECTED".equals(dto.getAction()) ? dto.getRemark() : null;
        int rows = petInfoMapper.updateStatusIfPending(petId, newStatus, "PENDING", remark);
        if (rows == 0) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST,
                    "该宠物已被其他志愿者审核，请刷新后查看");
        }

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
            // 通知所有管理员：有宠物待终审
            List<Long> adminIds = userRoleMapper.selectUserIdsByRoleCode("ADMIN");
            for (Long adminId : adminIds) {
                eventPublisher.publishEvent(new NotificationEvent(
                        adminId, "PET_REVIEW",
                        "新宠物待终审",
                        petName + "已通过初审，请尽快终审",
                        petId));
            }
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

        // 使用乐观锁：仅当 status = 'FIRST_PASS' 时更新
        String newStatus = "APPROVED".equals(dto.getAction()) ? "APPROVED" : "REJECTED";
        String remark = "REJECTED".equals(dto.getAction()) ? dto.getRemark() : null;
        int rows = petInfoMapper.updateStatusIfPending(petId, newStatus, "FIRST_PASS", remark);
        if (rows == 0) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST,
                    "该宠物状态已变更，请刷新后重试");
        }

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