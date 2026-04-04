package com.edu.platform.mapper;

import com.edu.platform.domain.EmailVerification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface EmailVerificationMapper {
    void insert(EmailVerification verification);
    EmailVerification findByToken(@Param("token") String token);
    void markUsed(@Param("token") String token);
    void deleteUnusedByEmail(@Param("email") String email);
}
