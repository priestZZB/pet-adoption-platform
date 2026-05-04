package com.pet.module.pet.service.impl;

import com.pet.common.enums.ResultCodeEnum;
import com.pet.common.exception.BusinessException;
import com.pet.module.pet.mapper.PetInfoMapper;
import com.pet.module.pet.mapper.PetReviewRecordMapper;
import com.pet.module.pet.model.dto.PetReviewDto;
import com.pet.module.pet.model.entity.PetInfo;
import com.pet.module.pet.model.entity.PetReviewRecord;
import com.pet.module.pet.service.PetReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PetReviewServiceImpl implements PetReviewService {

    @Autowired
    private PetInfoMapper petInfoMapper;

    @Autowired
    private PetReviewRecordMapper petReviewRecordMapper;

    @Override
    @Transactional
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
            update.setStatus("APPROVED");
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
    }

    @Override
    @Transactional
    public void finalReview(Long adminId, Long petId, PetReviewDto dto) {
        PetInfo pet = petInfoMapper.selectById(petId);
        if (pet == null) {
            throw new BusinessException(ResultCodeEnum.PET_NOT_FOUND);
        }
        if (!"APPROVED".equals(pet.getStatus())) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "该宠物尚未通过初审或非待终审状态");
        }

        PetInfo update = new PetInfo();
        update.setId(petId);
        if ("APPROVED".equals(dto.getAction())) {
            update.setStatus("APPROVED");
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
    }
}