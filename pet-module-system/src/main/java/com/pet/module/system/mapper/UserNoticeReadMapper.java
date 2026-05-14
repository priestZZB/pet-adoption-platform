package com.pet.module.system.mapper;

import com.pet.module.system.model.entity.UserNoticeRead;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface UserNoticeReadMapper {

    int insert(UserNoticeRead record);

    UserNoticeRead selectByUserAndNotice(@Param("userId") Long userId, @Param("noticeId") Long noticeId);

    List<Long> selectReadNoticeIdsByUserId(@Param("userId") Long userId);
}
