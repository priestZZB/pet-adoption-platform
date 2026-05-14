package com.pet.module.system.service.impl;

import com.pet.common.enums.ResultCodeEnum;
import com.pet.common.exception.BusinessException;
import com.pet.module.system.mapper.NoticeMapper;
import com.pet.module.system.mapper.UserNoticeReadMapper;
import com.pet.module.system.model.entity.SysNotice;
import com.pet.module.system.model.entity.UserNoticeRead;
import com.pet.module.system.model.vo.NoticeVo;
import com.pet.module.system.service.NoticeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class NoticeServiceImpl implements NoticeService {

    @Autowired
    private NoticeMapper noticeMapper;

    @Override
    public List<NoticeVo> getPublicNotices() {
        return noticeMapper.selectList().stream().map(n -> {
            NoticeVo vo = new NoticeVo();
            BeanUtils.copyProperties(n, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public NoticeVo getById(Long id) {
        SysNotice notice = noticeMapper.selectById(id);
        if (notice == null) {
            throw new BusinessException(ResultCodeEnum.NOTICE_NOT_FOUND);
        }
        NoticeVo vo = new NoticeVo();
        BeanUtils.copyProperties(notice, vo);
        return vo;
    }

    @Override
    public void add(String title, String content) {
        SysNotice notice = new SysNotice();
        notice.setTitle(title);
        notice.setContent(content);
        notice.setStatus(1);
        noticeMapper.insert(notice);
    }

    @Override
    public void update(Long id, String title, String content, Integer status) {
        SysNotice notice = noticeMapper.selectById(id);
        if (notice == null) {
            throw new BusinessException(ResultCodeEnum.NOTICE_NOT_FOUND);
        }
        SysNotice update = new SysNotice();
        update.setId(id);
        update.setTitle(title);
        update.setContent(content);
        update.setStatus(status);
        noticeMapper.updateById(update);
    }

    @Override
    public void delete(Long id) {
        if (noticeMapper.selectById(id) == null) {
            throw new BusinessException(ResultCodeEnum.NOTICE_NOT_FOUND);
        }
        noticeMapper.deleteById(id);
    }

    @Override
    public List<SysNotice> getAllForAdmin() {
        return noticeMapper.selectAll();
    }

    @Override
    public List<NoticeVo> getUnreadNotices(Long userId) {
        List<Long> readIds = userNoticeReadMapper.selectReadNoticeIdsByUserId(userId);
        Set<Long> readSet = readIds.stream().collect(Collectors.toSet());
        return noticeMapper.selectList().stream()
                .filter(n -> n.getStatus() == 1 && !readSet.contains(n.getId()))
                .map(n -> {
                    NoticeVo vo = new NoticeVo();
                    BeanUtils.copyProperties(n, vo);
                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void markAsRead(Long userId, Long noticeId) {
        UserNoticeRead existing = userNoticeReadMapper.selectByUserAndNotice(userId, noticeId);
        if (existing == null) {
            UserNoticeRead record = new UserNoticeRead();
            record.setUserId(userId);
            record.setNoticeId(noticeId);
            userNoticeReadMapper.insert(record);
        }
    }

    @Autowired
    private UserNoticeReadMapper userNoticeReadMapper;
}