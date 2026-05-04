package com.pet.module.system.mapper;

import com.pet.module.system.model.entity.SysNotice;
import java.util.List;

public interface NoticeMapper {

    int insert(SysNotice notice);

    int updateById(SysNotice notice);

    int deleteById(Long id);

    SysNotice selectById(Long id);

    List<SysNotice> selectList();

    List<SysNotice> selectAll();
}