package com.edu.platform.mapper;

import com.edu.platform.domain.PasswordResetToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PasswordResetTokenMapper {
    void insert(PasswordResetToken token);
    PasswordResetToken findByToken(@Param("token") String token);
    void markUsed(@Param("token") String token);
    void deleteExpiredByUserId(@Param("userId") Long userId);
}
