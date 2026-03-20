package com.edu.platform.mapper;

import com.edu.platform.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Optional;

@Mapper
public interface UserMapper {
    Optional<User> findByEmail(@Param("email") String email);
    Optional<User> findById(@Param("userId") Long userId);
    boolean existsByEmail(@Param("email") String email);
    List<User> findAll(@Param("keyword") String keyword, @Param("role") String role,
                       @Param("offset") int offset, @Param("limit") int limit);
    long countAll(@Param("keyword") String keyword, @Param("role") String role);
    void insert(User user);
    void update(User user);
    void updateLastLogin(@Param("userId") Long userId);
    void updateActive(@Param("userId") Long userId, @Param("isActive") boolean isActive);
    void softDelete(@Param("userId") Long userId);
    void updatePassword(@Param("userId") Long userId, @Param("passwordHash") String passwordHash);
    void updateNotificationSettings(User user);
    long countByRole(@Param("role") String role);
    long countByIsActive(@Param("isActive") boolean isActive);
    long count();
}
