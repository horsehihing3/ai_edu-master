package com.edu.platform.mapper;

import com.edu.platform.domain.Payment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface PaymentMapper {

    List<Payment> findAll(@Param("keyword") String keyword,
                          @Param("status") String status,
                          @Param("offset") int offset,
                          @Param("limit") int limit);

    long countAll(@Param("keyword") String keyword,
                  @Param("status") String status);

    Map<String, Object> getStats();
}
