package com.pet.module.system.mapper;

import com.pet.module.system.model.entity.Notification;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface NotificationMapper {

    int insert(Notification notification);

    List<Notification> selectByUserId(@Param("userId") Long userId);

    int countUnreadByUserId(@Param("userId") Long userId);

    int markAsRead(@Param("id") Long id, @Param("userId") Long userId);

    int markAllAsRead(@Param("userId") Long userId);
}
