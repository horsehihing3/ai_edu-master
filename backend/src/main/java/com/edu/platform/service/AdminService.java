package com.edu.platform.service;

import com.edu.platform.domain.Problem;
import com.edu.platform.domain.User;
import com.edu.platform.dto.admin.AdminDashboardDto;
import com.edu.platform.dto.common.PageResponse;
import com.edu.platform.exception.BusinessException;
import com.edu.platform.exception.ErrorCode;
import com.edu.platform.mapper.InquiryMapper;
import com.edu.platform.mapper.LearningSessionMapper;
import com.edu.platform.mapper.ProblemMapper;
import com.edu.platform.mapper.SchoolMapper;
import com.edu.platform.mapper.StudentMapper;
import com.edu.platform.mapper.TeacherMapper;
import com.edu.platform.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserMapper userMapper;
    private final StudentMapper studentMapper;
    private final TeacherMapper teacherMapper;
    private final SchoolMapper schoolMapper;
    private final ProblemMapper problemMapper;
    private final LearningSessionMapper learningSessionMapper;
    private final InquiryMapper inquiryMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public AdminDashboardDto getDashboard() {
        long totalStudents = userMapper.countByRole(User.UserRole.STUDENT.name());
        long totalTeachers = userMapper.countByRole(User.UserRole.TEACHER.name());
        long totalSchools = schoolMapper.count();
        long totalProblems = problemMapper.count();
        long activeUsers = userMapper.countByIsActive(true);

        return AdminDashboardDto.builder()
                .totalStudents(totalStudents)
                .totalTeachers(totalTeachers)
                .totalSchools(totalSchools)
                .dau(0L)
                .mau(0L)
                .dailySolvedCount(0L)
                .totalProblems(totalProblems)
                .activeUsers(activeUsers)
                .averageAccuracy(0.0)
                .build();
    }

    @Transactional(readOnly = true)
    public PageResponse<Map<String, Object>> getUserList(Map<String, Object> searchParams, int page, int size) {
        int offset = page * size;
        int limit = size;

        String keyword = searchParams != null ? (String) searchParams.get("keyword") : null;
        String role = searchParams != null ? (String) searchParams.get("role") : null;

        List<User> users = userMapper.findAll(keyword, role, offset, limit);
        long totalElements = userMapper.countAll(keyword, role);
        int totalPages = (int) Math.ceil((double) totalElements / limit);

        List<Map<String, Object>> content = users.stream()
                .map(u -> Map.<String, Object>of(
                        "userId", u.getUserId(),
                        "email", u.getEmail(),
                        "name", u.getName(),
                        "role", u.getRole() != null ? u.getRole() : "",
                        "isActive", u.getIsActive() != null ? u.getIsActive() : false,
                        "isEmailVerified", u.getIsEmailVerified() != null ? u.getIsEmailVerified() : false,
                        "createdAt", u.getCreatedAt() != null ? u.getCreatedAt().toString() : ""
                ))
                .collect(Collectors.toList());

        return PageResponse.of(content, totalElements, totalPages, page, limit);
    }

    @Transactional
    public void updateUserStatus(Long userId, boolean isActive) {
        userMapper.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        userMapper.updateActive(userId, isActive);
        log.info("User {} status updated to: {}", userId, isActive);
    }

    @Transactional
    public void resetUserPassword(Long userId) {
        userMapper.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String tempPassword = UUID.randomUUID().toString().substring(0, 12);
        userMapper.updatePassword(userId, passwordEncoder.encode(tempPassword));

        // TODO: 이메일로 임시 비밀번호 발송
        log.info("Password reset for user: {}", userId);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getSystemAnalytics() {
        long totalUsers = userMapper.count();
        long totalStudents = userMapper.countByRole(User.UserRole.STUDENT.name());
        long totalTeachers = userMapper.countByRole(User.UserRole.TEACHER.name());

        return Map.of(
                "totalUsers", totalUsers,
                "totalStudents", totalStudents,
                "totalTeachers", totalTeachers,
                "totalSchools", schoolMapper.count(),
                "totalProblems", problemMapper.count()
        );
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardStats() {
        AdminDashboardDto dto = getDashboard();
        Map<String, Object> result = new HashMap<>();
        result.put("dau", dto.getDau());
        result.put("dauTrend", 0);
        result.put("mau", dto.getMau());
        result.put("mauTrend", 0);
        result.put("dailySolved", dto.getDailySolvedCount());
        result.put("solvedTrend", 0);
        result.put("activeSubscribers", dto.getActiveUsers());
        result.put("subscriberTrend", 0);
        return result;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getDauData(int days) {
        List<Map<String, Object>> dbData = learningSessionMapper.getDailySessionCounts(days);
        // Build a map of date -> count from DB results
        Map<String, Long> dateCountMap = new HashMap<>();
        for (Map<String, Object> row : dbData) {
            String date = (String) row.get("date");
            Object countObj = row.get("count");
            long count = countObj instanceof Number ? ((Number) countObj).longValue() : 0L;
            if (date != null) dateCountMap.put(date, count);
        }
        // Fill all days in range
        List<Map<String, Object>> result = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/dd");
        for (int i = days - 1; i >= 0; i--) {
            String dateStr = LocalDate.now().minusDays(i).format(fmt);
            Map<String, Object> entry = new HashMap<>();
            entry.put("date", dateStr);
            entry.put("value", dateCountMap.getOrDefault(dateStr, 0L));
            result.add(entry);
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getLevelDistribution() {
        List<Map<String, Object>> rawData = studentMapper.getLevelDistribution();
        long total = rawData.stream()
                .mapToLong(r -> r.get("count") instanceof Number ? ((Number) r.get("count")).longValue() : 0L)
                .sum();
        return rawData.stream().map(r -> {
            String level = r.get("level") != null ? r.get("level").toString() : "?";
            long count = r.get("count") instanceof Number ? ((Number) r.get("count")).longValue() : 0L;
            long pct = total > 0 ? Math.round(count * 100.0 / total) : 0L;
            Map<String, Object> entry = new HashMap<>();
            entry.put("level", level);
            entry.put("count", count);
            entry.put("pct", pct);
            return entry;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getWeakUnitStats() {
        return problemMapper.getWeakUnitStats().stream().map(r -> {
            Map<String, Object> entry = new HashMap<>();
            entry.put("unit", r.get("unitName") != null ? r.get("unitName").toString() : "");
            Object acc = r.get("accuracy");
            entry.put("accuracy", acc instanceof Number ? ((Number) acc).doubleValue() : 0.0);
            return entry;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAdminInquiries(String status, int page, int size) {
        int offset = page * size;
        List<Map<String, Object>> items = inquiryMapper.findAllForAdmin(status, offset, size);
        long total = inquiryMapper.countAll(status);
        int totalPages = (int) Math.ceil((double) total / size);

        Map<String, Object> result = new HashMap<>();
        result.put("content", items);
        result.put("totalElements", total);
        result.put("totalPages", totalPages);
        result.put("page", page);
        result.put("size", size);
        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getInquiryDetail(Long inquiryId) {
        com.edu.platform.domain.Inquiry inquiry = inquiryMapper.findById(inquiryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INQUIRY_NOT_FOUND));
        List<com.edu.platform.domain.InquiryReply> replies = inquiryMapper.findRepliesByInquiryId(inquiryId);

        Map<String, Object> result = new HashMap<>();
        result.put("inquiryId", inquiry.getInquiryId());
        result.put("userId", inquiry.getUserId());
        result.put("category", inquiry.getCategory());
        result.put("title", inquiry.getTitle());
        result.put("content", inquiry.getContent());
        result.put("status", inquiry.getStatus());
        result.put("isSecret", inquiry.getIsSecret());
        result.put("createdAt", inquiry.getCreatedAt() != null ? inquiry.getCreatedAt().toString() : null);
        result.put("replies", replies.stream().map(r -> {
            Map<String, Object> rm = new HashMap<>();
            rm.put("replyId", r.getReplyId());
            rm.put("content", r.getContent());
            rm.put("createdAt", r.getCreatedAt() != null ? r.getCreatedAt().toString() : null);
            return rm;
        }).collect(Collectors.toList()));
        return result;
    }

    @Transactional(readOnly = true)
    public PageResponse<Map<String, Object>> getAdminProblems(String approvalStatus, String level, String keyword, int page, int size) {
        int offset = page * size;
        List<Problem> problems = problemMapper.searchAdmin(approvalStatus, level, keyword, offset, size);
        long total = problemMapper.countSearchAdmin(approvalStatus, level, keyword);
        int totalPages = (int) Math.ceil((double) total / size);

        List<Map<String, Object>> content = problems.stream().map(p -> {
            Map<String, Object> m = new HashMap<>();
            m.put("problemId", p.getProblemId());
            m.put("questionText", p.getQuestionText());
            m.put("level", p.getLevel());
            m.put("grade", p.getGrade());
            m.put("difficulty", p.getDifficulty());
            m.put("unitPath", p.getUnitPath());
            m.put("approvalStatus", p.getApprovalStatus());
            m.put("rejectReason", p.getRejectReason());
            m.put("isActive", p.getIsActive());
            m.put("createdAt", p.getCreatedAt() != null ? p.getCreatedAt().toString() : null);
            return m;
        }).collect(Collectors.toList());

        return PageResponse.of(content, total, totalPages, page, size);
    }

    @Transactional
    public void approveProblem(Long problemId, Long adminUserId) {
        problemMapper.findById(problemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROBLEM_NOT_FOUND));
        problemMapper.updateApprovalStatus(problemId, "APPROVED", adminUserId, null);
        log.info("Problem {} approved by admin {}", problemId, adminUserId);
    }

    @Transactional
    public void rejectProblem(Long problemId, Long adminUserId, String rejectReason) {
        problemMapper.findById(problemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROBLEM_NOT_FOUND));
        problemMapper.updateApprovalStatus(problemId, "REJECTED", adminUserId, rejectReason);
        log.info("Problem {} rejected by admin {}, reason: {}", problemId, adminUserId, rejectReason);
    }

    @Transactional
    public void replyToInquiry(Long inquiryId, Long adminUserId, String content) {
        inquiryMapper.findById(inquiryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INQUIRY_NOT_FOUND));

        com.edu.platform.domain.InquiryReply reply = com.edu.platform.domain.InquiryReply.builder()
                .inquiryId(inquiryId)
                .replierId(adminUserId)
                .content(content)
                .build();
        inquiryMapper.insertReply(reply);
        inquiryMapper.updateStatus(inquiryId, "ANSWERED");
        log.info("Admin reply added to inquiry: {}", inquiryId);
    }
}
