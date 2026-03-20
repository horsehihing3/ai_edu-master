package com.edu.platform.mapper;

import com.edu.platform.domain.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface NotificationMapper {
    List<Notification> findByUserId(@Param("userId") Long userId,
                                     @Param("offset") int offset, @Param("limit") int limit);
    long countUnreadByUserId(@Param("userId") Long userId);
    void insert(Notification notification);
    void markAsRead(@Param("notificationId") Long notificationId);
    void markAllAsRead(@Param("userId") Long userId);
}
