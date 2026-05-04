package com.pet.module.system.service;

import com.pet.module.system.model.entity.SysNotice;
import com.pet.module.system.model.vo.NoticeVo;
import java.util.List;

public interface NoticeService {

    List<NoticeVo> getPublicNotices();

    NoticeVo getById(Long id);

    void add(String title, String content);

    void update(Long id, String title, String content, Integer status);

    void delete(Long id);

    List<SysNotice> getAllForAdmin();
}