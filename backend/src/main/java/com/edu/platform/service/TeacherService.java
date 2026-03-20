package com.edu.platform.service;

import com.edu.platform.domain.Assignment;
import com.edu.platform.domain.LearningSession;
import com.edu.platform.domain.Student;
import com.edu.platform.domain.Teacher;
import com.edu.platform.domain.User;
import com.edu.platform.dto.common.PageResponse;
import com.edu.platform.dto.teacher.AssignmentCreateRequest;
import com.edu.platform.dto.teacher.AssignmentListDto;
import com.edu.platform.exception.BusinessException;
import com.edu.platform.exception.ErrorCode;
import com.edu.platform.mapper.AssignmentMapper;
import com.edu.platform.mapper.LearningSessionMapper;
import com.edu.platform.mapper.StudentMapper;
import com.edu.platform.mapper.TeacherMapper;
import com.edu.platform.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherMapper teacherMapper;
    private final StudentMapper studentMapper;
    private final UserMapper userMapper;
    private final AssignmentMapper assignmentMapper;
    private final LearningSessionMapper learningSessionMapper;

    @Transactional(readOnly = true)
    public PageResponse<Map<String, Object>> getClassStudents(Long teacherId, int page, int size) {
        Teacher teacher = teacherMapper.findById(teacherId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEACHER_NOT_FOUND));

        int offset = page * size;
        List<Map<String, Object>> content = studentMapper.getListWithStats(teacher.getSchoolId(), offset, size);
        long totalElements = studentMapper.countBySchoolIdForStats(teacher.getSchoolId());
        int totalPages = (int) Math.ceil((double) totalElements / size);

        return PageResponse.of(content, totalElements, totalPages, page, size);
    }

    @Transactional
    public Long createAssignment(Long teacherId, AssignmentCreateRequest request) {
        Teacher teacher = teacherMapper.findById(teacherId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEACHER_NOT_FOUND));

        Assignment assignment = Assignment.builder()
                .teacherId(teacherId)
                .schoolId(teacher.getSchoolId())
                .title(request.getTitle())
                .description(request.getDescription())
                .targetType(request.getTargetType() != null ? request.getTargetType().name() : null)
                .targetLevel(request.getTargetLevel() != null ? request.getTargetLevel().name() : null)
                .dueDate(request.getDueDate())
                .notifyEmail(request.getNotifyEmail() != null ? request.getNotifyEmail() : false)
                .isAutoAssign(request.getIsAutoAssign() != null ? request.getIsAutoAssign() : false)
                .build();

        assignmentMapper.insert(assignment);

        // 문제 배정
        if (request.getProblemIds() != null) {
            for (int i = 0; i < request.getProblemIds().size(); i++) {
                assignmentMapper.insertProblem(assignment.getAssignmentId(), request.getProblemIds().get(i), i + 1);
            }
        }

        // 학생 배정
        if (request.getStudentIds() != null) {
            for (Long studentId : request.getStudentIds()) {
                assignmentMapper.insertTarget(assignment.getAssignmentId(), null, studentId);
            }
        }

        log.info("Assignment created: {} by teacher: {}", assignment.getAssignmentId(), teacherId);
        return assignment.getAssignmentId();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAssignmentDetail(Long assignmentId) {
        Map<String, Object> detail = assignmentMapper.getDetail(assignmentId);
        if (detail == null) throw new BusinessException(ErrorCode.ASSIGNMENT_NOT_FOUND);
        // compute status from dueDate
        Object dueDate = detail.get("dueDate");
        detail.put("status", computeStatus(dueDate instanceof java.time.LocalDateTime
                ? (java.time.LocalDateTime) dueDate : null));
        // completionRate
        long targetCount = toLong(detail.get("targetCount"));
        List<Map<String, Object>> progress = assignmentMapper.getStudentProgress(assignmentId);
        long completed = progress.stream().filter(p -> toLong(p.get("completed")) == 1).count();
        detail.put("completionRate", targetCount > 0 ? Math.round(completed * 100.0 / targetCount) : 0);
        return detail;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAssignmentProgress(Long assignmentId) {
        assignmentMapper.findById(assignmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ASSIGNMENT_NOT_FOUND));
        return assignmentMapper.getStudentProgress(assignmentId);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getStudentProgress(Long studentId, Long teacherId) {
        Student student = studentMapper.findById(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));
        User user = userMapper.findById(student.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<LearningSession> sessions = learningSessionMapper.findAllByStudentId(studentId);
        int totalSolved = sessions.stream().mapToInt(s -> s.getSolvedCount() != null ? s.getSolvedCount() : 0).sum();
        int totalCorrect = sessions.stream().mapToInt(s -> s.getCorrectCount() != null ? s.getCorrectCount() : 0).sum();
        double accuracy = totalSolved > 0 ? (double) totalCorrect / totalSolved * 100 : 0;

        return Map.of(
                "studentId", studentId,
                "studentName", user.getName(),
                "studentLevel", student.getStudentLevel() != null ? student.getStudentLevel() : "N/A",
                "totalSolved", totalSolved,
                "totalCorrect", totalCorrect,
                "accuracy", accuracy,
                "completedAssignments", sessions.stream()
                        .filter(s -> "COMPLETED".equals(s.getStatus())).count()
        );
    }

    @Transactional
    public void sendAssignmentNotification(Long assignmentId) {
        assignmentMapper.findById(assignmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ASSIGNMENT_NOT_FOUND));

        // TODO: 실제 알림 발송 로직 구현
        log.info("Assignment notification sent for: {}", assignmentId);
    }

    @Transactional(readOnly = true)
    public PageResponse<Map<String, Object>> getAssignmentList(Long teacherId, int page, int size) {
        int offset = page * size;

        List<Map<String, Object>> rows = assignmentMapper.getListWithStats(teacherId, offset, size);
        long totalElements = assignmentMapper.countByTeacherId(teacherId);
        int totalPages = (int) Math.ceil((double) totalElements / size);

        List<Map<String, Object>> content = rows.stream().map(row -> {
            long targetCount = toLong(row.get("targetCount"));
            long completedCount = toLong(row.get("completedCount"));
            double rate = targetCount > 0 ? Math.round(completedCount * 100.0 / targetCount) : 0;

            Object dueDate = row.get("dueDate");
            java.time.LocalDateTime dueDt = dueDate instanceof java.time.LocalDateTime
                    ? (java.time.LocalDateTime) dueDate : null;

            Map<String, Object> m = new HashMap<>(row);
            m.put("completionRate", rate);
            m.put("targetCount", targetCount);
            m.put("status", computeStatus(dueDt));
            return m;
        }).collect(Collectors.toList());

        return PageResponse.of(content, totalElements, totalPages, page, size);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getTeacherDashboardStats(Long teacherId) {
        Teacher teacher = teacherMapper.findById(teacherId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEACHER_NOT_FOUND));

        List<Student> students = studentMapper.findBySchoolIdAll(teacher.getSchoolId());
        long activeAssignments = assignmentMapper.countByTeacherId(teacherId);

        Map<String, Object> result = new HashMap<>();
        result.put("studentCount", students.size());
        result.put("studentChange", "");
        result.put("activeAssignments", activeAssignments);
        result.put("avgCompletionRate", 0);
        result.put("completionChange", "");
        result.put("incompleteCount", 0);
        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getTeacherAnalytics(Long teacherId) {
        Teacher teacher = teacherMapper.findById(teacherId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEACHER_NOT_FOUND));
        Long schoolId = teacher.getSchoolId();

        List<Student> students = studentMapper.findBySchoolIdAll(schoolId);
        long totalStudents = students.size();

        // 레벨별 분포
        long levelA = students.stream().filter(s -> "A".equals(s.getStudentLevel())).count();
        long levelB = students.stream().filter(s -> "B".equals(s.getStudentLevel())).count();
        long levelC = students.stream().filter(s -> "C".equals(s.getStudentLevel())).count();
        long pctA = totalStudents > 0 ? Math.round(levelA * 100.0 / totalStudents) : 0;
        long pctB = totalStudents > 0 ? Math.round(levelB * 100.0 / totalStudents) : 0;
        long pctC = totalStudents > 0 ? Math.round(levelC * 100.0 / totalStudents) : 0;
        List<Map<String, Object>> levelDist = new ArrayList<>();
        Map<String, Object> la = new HashMap<>(); la.put("level", "A"); la.put("count", levelA); la.put("pct", pctA); levelDist.add(la);
        Map<String, Object> lb = new HashMap<>(); lb.put("level", "B"); lb.put("count", levelB); lb.put("pct", pctB); levelDist.add(lb);
        Map<String, Object> lc = new HashMap<>(); lc.put("level", "C"); lc.put("count", levelC); lc.put("pct", pctC); levelDist.add(lc);

        // 과목별 정답률 / 주간 참여율 / 학생별 통계
        List<Map<String, Object>> subjectStats  = teacherMapper.getSubjectStats(schoolId);
        List<Map<String, Object>> weeklyData    = teacherMapper.getWeeklyParticipation(schoolId);
        List<Map<String, Object>> studentStats  = teacherMapper.getStudentStats(schoolId);

        // KPI 집계
        double avgWeekStudy = studentStats.stream()
                .mapToLong(s -> toLong(s.get("weekStudy"))).average().orElse(0);
        double avgCorrectRate = studentStats.stream()
                .mapToLong(s -> toLong(s.get("correctRate"))).average().orElse(0);
        double avgAssignRate = studentStats.stream()
                .mapToLong(s -> toLong(s.get("assignRate"))).average().orElse(0);

        List<Map<String, Object>> kpis = new ArrayList<>();
        Map<String, Object> k1 = new HashMap<>(); k1.put("value", totalStudents + "명");  k1.put("sub", ""); kpis.add(k1);
        Map<String, Object> k2 = new HashMap<>(); k2.put("value", Math.round(avgWeekStudy) + "문제"); k2.put("sub", ""); kpis.add(k2);
        Map<String, Object> k3 = new HashMap<>(); k3.put("value", Math.round(avgCorrectRate) + "%"); k3.put("sub", ""); kpis.add(k3);
        Map<String, Object> k4 = new HashMap<>(); k4.put("value", Math.round(avgAssignRate) + "%"); k4.put("sub", ""); kpis.add(k4);

        Map<String, Object> result = new HashMap<>();
        result.put("kpis", kpis);
        result.put("levelDist", levelDist);
        result.put("subjectStats", subjectStats);
        result.put("weeklyData", weeklyData);
        result.put("studentStats", studentStats);
        return result;
    }

    private String computeStatus(java.time.LocalDateTime dueDate) {
        if (dueDate == null) return "DRAFT";
        return dueDate.isBefore(java.time.LocalDateTime.now()) ? "DONE" : "ACTIVE";
    }

    private long toLong(Object val) {
        if (val == null) return 0L;
        if (val instanceof Number) return ((Number) val).longValue();
        try { return Long.parseLong(val.toString()); } catch (Exception e) { return 0L; }
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getStudentInfo(Long studentId) {
        Student student = studentMapper.findById(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));
        User user = userMapper.findById(student.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Map<String, Object> result = new HashMap<>();
        result.put("studentId", studentId);
        result.put("name", user.getName());
        result.put("email", user.getEmail());
        result.put("grade", student.getGrade() != null ? student.getGrade() : "");
        result.put("studentLevel", student.getStudentLevel() != null ? student.getStudentLevel() : "B");
        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getStudentStats(Long studentId) {
        studentMapper.findById(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));
        List<LearningSession> sessions = learningSessionMapper.findAllByStudentId(studentId);
        int totalSolved  = sessions.stream().mapToInt(s -> s.getSolvedCount()  != null ? s.getSolvedCount()  : 0).sum();
        int totalCorrect = sessions.stream().mapToInt(s -> s.getCorrectCount() != null ? s.getCorrectCount() : 0).sum();
        long accuracy = totalSolved > 0 ? Math.round((double) totalCorrect / totalSolved * 100) : 0;
        long completed = sessions.stream().filter(s -> "COMPLETED".equals(s.getStatus())).count();
        long completionRate = !sessions.isEmpty() ? Math.round((double) completed / sessions.size() * 100) : 0;
        long studyDays = sessions.stream()
                .filter(s -> s.getStartedAt() != null)
                .map(s -> s.getStartedAt().toLocalDate())
                .distinct().count();
        Map<String, Object> result = new HashMap<>();
        result.put("totalSolved", totalSolved);
        result.put("accuracy", accuracy);
        result.put("completionRate", completionRate);
        result.put("studyDays", studyDays);
        return result;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getStudentHistory(Long studentId, int size) {
        List<LearningSession> sessions = learningSessionMapper.findLatestByStudentId(studentId, 0, size);
        return sessions.stream().map(s -> {
            Map<String, Object> entry = new HashMap<>();
            entry.put("date", s.getStartedAt() != null ? s.getStartedAt().toString() : null);
            String title = "-";
            if (s.getAssignmentId() != null) {
                try {
                    title = assignmentMapper.findById(s.getAssignmentId()).map(Assignment::getTitle).orElse("-");
                } catch (Exception ignored) {}
            }
            entry.put("assignmentTitle", title);
            entry.put("problemCount", s.getTotalProblems() != null ? s.getTotalProblems() : 0);
            int solved = s.getSolvedCount() != null ? s.getSolvedCount() : 0;
            int correct = s.getCorrectCount() != null ? s.getCorrectCount() : 0;
            entry.put("accuracy", solved > 0 ? Math.round((double) correct / solved * 100) : 0);
            long mins = 0;
            if (s.getStartedAt() != null && s.getCompletedAt() != null) {
                mins = java.time.Duration.between(s.getStartedAt(), s.getCompletedAt()).toMinutes();
            }
            entry.put("timeSpentMin", mins > 0 ? mins : null);
            return entry;
        }).collect(Collectors.toList());
    }

    @Transactional
    public void changeStudentLevel(Long studentId, String level) {
        studentMapper.findById(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));
        studentMapper.updateLevel(studentId, level);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getIncompleteStudents(Long teacherId, int size) {
        Teacher teacher = teacherMapper.findById(teacherId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEACHER_NOT_FOUND));

        List<Student> students = studentMapper.findBySchoolIdAll(teacher.getSchoolId());
        return students.stream().limit(size).map(s -> {
            User user = userMapper.findById(s.getUserId()).orElse(null);
            Map<String, Object> entry = new HashMap<>();
            entry.put("studentId", s.getStudentId());
            entry.put("name", user != null ? user.getName() : "Unknown");
            entry.put("grade", s.getGrade() != null ? s.getGrade() : "");
            entry.put("level", s.getStudentLevel() != null ? s.getStudentLevel() : "N/A");
            entry.put("lastLogin", "");
            return entry;
        }).collect(Collectors.toList());
    }
}
