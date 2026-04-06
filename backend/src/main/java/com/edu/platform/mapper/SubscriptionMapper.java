package com.edu.platform.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mapper
public interface SubscriptionMapper {
    // [2026-04-06] 학생 현재 활성 구독 조회
    Optional<Map<String, Object>> findActiveByUserId(@Param("userId") Long userId);

    // [2026-04-06] 학생 최근 결제 이력 (최근 5건)
    List<Map<String, Object>> findRecentPaymentsByUserId(@Param("userId") Long userId, @Param("limit") int limit);
}
