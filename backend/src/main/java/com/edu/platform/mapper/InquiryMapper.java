package com.edu.platform.mapper;

import com.edu.platform.domain.Inquiry;
import com.edu.platform.domain.InquiryReply;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mapper
public interface InquiryMapper {
    Optional<Inquiry> findById(@Param("inquiryId") Long inquiryId);
    List<Inquiry> findByUserId(@Param("userId") Long userId,
                                @Param("offset") int offset, @Param("limit") int limit);
    List<Inquiry> findAll(@Param("status") String status,
                           @Param("offset") int offset, @Param("limit") int limit);
    long countAll(@Param("status") String status);
    void insert(Inquiry inquiry);
    void updateStatus(@Param("inquiryId") Long inquiryId, @Param("status") String status);
    void insertReply(InquiryReply reply);
    List<InquiryReply> findRepliesByInquiryId(@Param("inquiryId") Long inquiryId);
    List<Map<String, Object>> findAllForAdmin(@Param("status") String status,
                                               @Param("offset") int offset, @Param("limit") int limit);
}
