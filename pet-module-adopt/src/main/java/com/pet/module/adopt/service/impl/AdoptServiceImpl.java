package com.pet.module.adopt.service.impl;

import com.github.pagehelper.PageHelper;
import com.pet.common.enums.ResultCodeEnum;
import com.pet.common.event.NotificationEvent;
import com.pet.common.exception.BusinessException;
import com.pet.module.adopt.mapper.AdoptApplicationMapper;
import com.pet.module.adopt.mapper.AdoptExamRecordMapper;
import com.pet.module.adopt.model.dto.AdoptApplyDto;
import com.pet.module.adopt.model.entity.AdoptApplication;
import com.pet.module.adopt.model.entity.AdoptExamRecord;
import com.pet.module.adopt.model.vo.AdoptApplyVo;
import com.pet.module.adopt.service.AdoptService;
import com.pet.module.pet.mapper.PetImageMapper;
import com.pet.module.pet.mapper.PetInfoMapper;
import com.pet.module.pet.model.entity.PetImage;
import com.pet.module.pet.model.entity.PetInfo;
import com.pet.module.system.mapper.UserMapper;
import com.pet.module.system.model.entity.SysUser;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdoptServiceImpl implements AdoptService {

    @Autowired
    private AdoptApplicationMapper adoptApplicationMapper;

    @Autowired
    private AdoptExamRecordMapper adoptExamRecordMapper;

    @Autowired
    private PetInfoMapper petInfoMapper;

    @Autowired
    private PetImageMapper petImageMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void apply(Long userId, AdoptApplyDto dto) {
        // 检查最近一次考试是否满分
        AdoptExamRecord latest = adoptExamRecordMapper.selectLatestByUserId(userId);
        if (latest == null || latest.getIsPassed() != 1) {
            throw new BusinessException(ResultCodeEnum.EXAM_NOT_PASS);
        }

        SysUser applier = userMapper.selectById(userId);
        if (applier == null || applier.getIsRealName() != 1) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "请先完成实名认证才能提交领养申请");
        }

        PetInfo pet = petInfoMapper.selectById(dto.getPetId());
        if (pet == null) {
            throw new BusinessException(ResultCodeEnum.PET_NOT_FOUND);
        }
        // 已领养的宠物不可申请
        if ("ADOPTED".equals(pet.getStatus())) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "该宠物已被领养");
        }

        // 不能领养自己发布的宠物
        if (pet.getUserId().equals(userId)) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "您不能领养自己发布的宠物");
        }

        // 同一用户不可对同一宠物重复提交申请
        if (adoptApplicationMapper.countActiveByUserAndPet(userId, dto.getPetId()) > 0) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "您已提交过该宠物的领养申请，请勿重复提交");
        }

        AdoptApplication application = new AdoptApplication();
        application.setUserId(userId);
        application.setPetId(dto.getPetId());
        application.setLivingEnv(dto.getLivingEnv());
        application.setPetExp(dto.getPetExp());
        application.setCommitment(dto.getCommitment());
        application.setStatus("PENDING");
        adoptApplicationMapper.insert(application);

        // 通知送养人有人申请领养他的宠物
        String applierName = applier.getNickname() != null ? applier.getNickname() : applier.getUsername();
        String petName = pet.getName() != null ? pet.getName() : "宠物";
        eventPublisher.publishEvent(new NotificationEvent(
                pet.getUserId(), "ADOPT_APPLY",
                "新领养申请",
                applierName + "申请领养你发布的" + petName + "，请尽快审核",
                application.getId()));
    }

    @Override
    public List<AdoptApplyVo> getMyApplications(Long userId) {
        List<AdoptApplication> list = adoptApplicationMapper.selectByUserId(userId);
        return list.stream().map(this::convertToVo).collect(Collectors.toList());
    }

    @Override
    public AdoptApplyVo getApplicationDetail(Long id) {
        AdoptApplication app = adoptApplicationMapper.selectById(id);
        if (app == null) {
            throw new BusinessException(ResultCodeEnum.APPLICATION_NOT_FOUND);
        }
        return convertToVo(app);
    }

    @Override
    public List<AdoptApplyVo> getAllApplications(String status, int page, int size) {
        PageHelper.startPage(page, size);
        List<AdoptApplication> list = adoptApplicationMapper.selectAll(status);
        return list.stream().map(this::convertToVo).collect(Collectors.toList());
    }

    @Override
    public AdoptApplyVo getAdminApplicationDetail(Long id) {
        return getApplicationDetail(id);
    }

    @Override
    @CacheEvict(cacheNames = "pet", allEntries = true)
    @Transactional
    public void adminReview(Long id, String action) {
        AdoptApplication app = adoptApplicationMapper.selectById(id);
        if (app == null) {
            throw new BusinessException(ResultCodeEnum.APPLICATION_NOT_FOUND);
        }
        AdoptApplication update = new AdoptApplication();
        update.setId(id);
        update.setStatus("APPROVED".equals(action) ? "APPROVED" : "REJECTED");
        adoptApplicationMapper.updateById(update);

        // 如果通过，更新宠物状态为已领养
        if ("APPROVED".equals(action)) {
            PetInfo petUpdate = new PetInfo();
            petUpdate.setId(app.getPetId());
            petUpdate.setStatus("ADOPTED");
            petInfoMapper.updateById(petUpdate);
        }

        // 通知申请人
        PetInfo pet = petInfoMapper.selectById(app.getPetId());
        String petName = pet != null && pet.getName() != null ? pet.getName() : "宠物";
        if ("APPROVED".equals(action)) {
            eventPublisher.publishEvent(new NotificationEvent(
                    app.getUserId(), "ADOPT_REVIEW",
                    "领养申请已通过",
                    "管理员已批准你领养" + petName + "的申请🎉",
                    id));
        } else {
            eventPublisher.publishEvent(new NotificationEvent(
                    app.getUserId(), "ADOPT_REVIEW",
                    "领养申请未通过",
                    "管理员驳回了你领养" + petName + "的申请",
                    id));
        }
    }

    @Override
    public List<AdoptApplyVo> getPetApplications(Long petId) {
        List<AdoptApplication> list = adoptApplicationMapper.selectByPetId(petId);
        return list.stream().map(this::convertToVo).collect(Collectors.toList());
    }

    @Override
    @CacheEvict(cacheNames = "pet", allEntries = true)
    @Transactional
    public void donorReview(Long id, String action) {
        AdoptApplication app = adoptApplicationMapper.selectById(id);
        if (app == null) {
            throw new BusinessException(ResultCodeEnum.APPLICATION_NOT_FOUND);
        }
        AdoptApplication update = new AdoptApplication();
        update.setId(id);
        update.setStatus("APPROVED".equals(action) ? "APPROVED" : "REJECTED");
        adoptApplicationMapper.updateById(update);

        // 如果送养人通过审核，同步更新宠物状态为已领养
        if ("APPROVED".equals(action)) {
            PetInfo petUpdate = new PetInfo();
            petUpdate.setId(app.getPetId());
            petUpdate.setStatus("ADOPTED");
            petInfoMapper.updateById(petUpdate);
        }

        // 通知申请人
        PetInfo pet = petInfoMapper.selectById(app.getPetId());
        String petName = pet != null && pet.getName() != null ? pet.getName() : "宠物";
        if ("APPROVED".equals(action)) {
            eventPublisher.publishEvent(new NotificationEvent(
                    app.getUserId(), "ADOPT_REVIEW",
                    "领养申请已通过",
                    "恭喜！你领养" + petName + "的申请已通过🎉",
                    id));
        } else {
            eventPublisher.publishEvent(new NotificationEvent(
                    app.getUserId(), "ADOPT_REVIEW",
                    "领养申请未通过",
                    "你领养" + petName + "的申请已被拒绝",
                    id));
        }
    }

    private AdoptApplyVo convertToVo(AdoptApplication app) {
        AdoptApplyVo vo = new AdoptApplyVo();
        BeanUtils.copyProperties(app, vo);

        // 用户信息
        SysUser user = userMapper.selectById(app.getUserId());
        if (user != null) {
            vo.setUserNickname(user.getNickname());
            vo.setUserAvatar(user.getAvatar());
            vo.setUserPhone(user.getPhone());
        }

        // 宠物信息
        PetInfo pet = petInfoMapper.selectById(app.getPetId());
        if (pet != null) {
            vo.setPetName(pet.getName());
            List<PetImage> images = petImageMapper.selectByPetId(pet.getId());
            if (!images.isEmpty()) {
                vo.setPetCoverImage(images.get(0).getImageUrl());
            }
        }

        return vo;
    }
}