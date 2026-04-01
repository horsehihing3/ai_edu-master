package com.edu.platform.controller;

import com.edu.platform.common.ResponseMessage;
import com.edu.platform.domain.Teacher;
import com.edu.platform.dto.common.ApiResponse;
import com.edu.platform.dto.common.PageResponse;
import com.edu.platform.dto.teacher.AssignmentCreateRequest;
import com.edu.platform.dto.teacher.UpdateAssignmentRequest;
import com.edu.platform.exception.BusinessException;
import com.edu.platform.exception.ErrorCode;
import com.edu.platform.mapper.TeacherMapper;
import com.edu.platform.mapper.UserMapper;
import com.edu.platform.service.FeedbackService;
import com.edu.platform.service.ProblemService;
import com.edu.platform.service.TeacherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/teacher")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;
    private final FeedbackService feedbackService;
    private final ProblemService problemService;
    private final UserMapper userMapper;
    private final TeacherMapper teacherMapper;

    @GetMapping("/home")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getHome(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        Teacher teacher = getTeacher(userId);
        Map<String, Object> home = Map.of(
                "teacherId", teacher.getTeacherId(),
                "schoolId", teacher.getSchoolId()
        );
        return ResponseEntity.ok(ApiResponse.success(home));
    }

    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardStats(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        Teacher teacher = getTeacher(userId);
        return ResponseEntity.ok(ApiResponse.success(teacherService.getTeacherDashboardStats(teacher.getTeacherId())));
    }

    @GetMapping("/analytics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAnalytics(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        Teacher teacher = getTeacher(userId);
        return ResponseEntity.ok(ApiResponse.success(teacherService.getTeacherAnalytics(teacher.getTeacherId())));
    }

    @GetMapping("/students/incomplete")
    public ResponseEntity<ApiResponse<Object>> getIncompleteStudents(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = getUserId(userDetails);
        Teacher teacher = getTeacher(userId);
        return ResponseEntity.ok(ApiResponse.success(teacherService.getIncompleteStudents(teacher.getTeacherId(), size)));
    }

    // [2026-04-01] 학생 리포트 CSV 내보내기
    @GetMapping("/students/export")
    public ResponseEntity<byte[]> exportStudents(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        Teacher teacher = getTeacher(userId);
        String csv = teacherService.exportStudentsCsv(teacher.getTeacherId());
        byte[] bytes = csv.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"students_report.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(bytes);
    }

    @GetMapping("/students")
    public ResponseEntity<ApiResponse<PageResponse<Map<String, Object>>>> getStudents(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = getUserId(userDetails);
        Teacher teacher = getTeacher(userId);
        PageResponse<Map<String, Object>> students = teacherService.getClassStudents(teacher.getTeacherId(), page, size);
        return ResponseEntity.ok(ApiResponse.success(students));
    }

    @GetMapping("/students/{studentId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStudentDetail(
            @PathVariable Long studentId) {
        return ResponseEntity.ok(ApiResponse.success(teacherService.getStudentInfo(studentId)));
    }

    @GetMapping("/students/{studentId}/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStudentStats(
            @PathVariable Long studentId) {
        return ResponseEntity.ok(ApiResponse.success(teacherService.getStudentStats(studentId)));
    }

    @GetMapping("/students/{studentId}/weak-units")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getStudentWeakUnits(
            @PathVariable Long studentId) {
        return ResponseEntity.ok(ApiResponse.success(List.of()));
    }

    @GetMapping("/students/{studentId}/history")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getStudentHistory(
            @PathVariable Long studentId,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(teacherService.getStudentHistory(studentId, size)));
    }

    @PutMapping("/students/{studentId}/level")
    public ResponseEntity<ApiResponse<Void>> changeStudentLevel(
            @PathVariable Long studentId,
            @RequestBody Map<String, String> body) {
        teacherService.changeStudentLevel(studentId, body.get("level"));
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/students/{studentId}/progress")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStudentProgress(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long studentId) {
        Long userId = getUserId(userDetails);
        Teacher teacher = getTeacher(userId);
        Map<String, Object> progress = teacherService.getStudentProgress(studentId, teacher.getTeacherId());
        return ResponseEntity.ok(ApiResponse.success(progress));
    }

    @PostMapping("/assignments")
    public ResponseEntity<ApiResponse<Long>> createAssignment(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AssignmentCreateRequest request) {
        Long userId = getUserId(userDetails);
        Teacher teacher = getTeacher(userId);
        Long assignmentId = teacherService.createAssignment(teacher.getTeacherId(), request);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.ASSIGNMENT_CREATED, assignmentId));
    }

    @GetMapping("/assignments")
    public ResponseEntity<ApiResponse<PageResponse<Map<String, Object>>>> getAssignments(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = getUserId(userDetails);
        Teacher teacher = getTeacher(userId);
        PageResponse<Map<String, Object>> assignments = teacherService.getAssignmentList(teacher.getTeacherId(), page, size);
        return ResponseEntity.ok(ApiResponse.success(assignments));
    }

    @GetMapping("/assignments/{assignmentId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAssignmentDetail(
            @PathVariable Long assignmentId) {
        return ResponseEntity.ok(ApiResponse.success(teacherService.getAssignmentDetail(assignmentId)));
    }

    @GetMapping("/assignments/{assignmentId}/problems")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAssignmentProblems(
            @PathVariable Long assignmentId) {
        return ResponseEntity.ok(ApiResponse.success(problemService.getProblemsForAssignment(assignmentId)));
    }

    @GetMapping("/assignments/{assignmentId}/progress")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAssignmentProgress(
            @PathVariable Long assignmentId) {
        return ResponseEntity.ok(ApiResponse.success(teacherService.getAssignmentProgress(assignmentId)));
    }

    @PutMapping("/assignments/{assignmentId}")
    @PreAuthorize("hasAnyRole('TEACHER','SUPER_USER')")
    public ResponseEntity<ApiResponse<Void>> updateAssignment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long assignmentId,
            @Valid @RequestBody UpdateAssignmentRequest request) {
        Long userId = getUserId(userDetails);
        Teacher teacher = getTeacher(userId);
        teacherService.updateAssignment(assignmentId, request, teacher.getTeacherId());
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.ASSIGNMENT_UPDATED));
    }

    @DeleteMapping("/assignments/{assignmentId}")
    @PreAuthorize("hasAnyRole('TEACHER','SUPER_USER')")
    public ResponseEntity<ApiResponse<Void>> deleteAssignment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long assignmentId) {
        Long userId = getUserId(userDetails);
        Teacher teacher = getTeacher(userId);
        teacherService.deleteAssignment(assignmentId, teacher.getTeacherId());
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.ASSIGNMENT_DELETED));
    }

    // 학생별 문제 풀이 상세 (정답/오답 + 기존 피드백)
    @GetMapping("/assignments/{assignmentId}/students/{studentId}/attempts")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getStudentAttempts(
            @PathVariable Long assignmentId, @PathVariable Long studentId) {
        return ResponseEntity.ok(ApiResponse.success(feedbackService.getStudentAttemptDetails(assignmentId, studentId)));
    }

    // 과제 전체 피드백 조회
    @GetMapping("/assignments/{assignmentId}/students/{studentId}/feedback")
    public ResponseEntity<ApiResponse<Object>> getAssignmentFeedback(
            @PathVariable Long assignmentId, @PathVariable Long studentId) {
        return ResponseEntity.ok(ApiResponse.success(feedbackService.getAssignmentFeedback(assignmentId, studentId)));
    }

    // 과제 전체 피드백 저장
    @PostMapping("/assignments/{assignmentId}/students/{studentId}/feedback")
    public ResponseEntity<ApiResponse<Object>> saveAssignmentFeedback(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long assignmentId, @PathVariable Long studentId,
            @RequestBody Map<String, String> request) {
        Long userId = getUserId(userDetails);
        Teacher teacher = getTeacher(userId);
        return ResponseEntity.ok(ApiResponse.success(feedbackService.saveAssignmentFeedback(
                assignmentId, studentId, teacher.getTeacherId(),
                request.get("comment"), request.get("drawingUrl"))));
    }

    // 문제별 피드백 저장
    @PostMapping("/attempts/{attemptId}/feedback")
    public ResponseEntity<ApiResponse<Object>> saveAttemptFeedback(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long attemptId,
            @RequestBody Map<String, String> request) {
        Long userId = getUserId(userDetails);
        Teacher teacher = getTeacher(userId);
        return ResponseEntity.ok(ApiResponse.success(feedbackService.saveAttemptFeedback(
                attemptId, teacher.getTeacherId(),
                request.get("comment"), request.get("drawingUrl"))));
    }

    private Long getUserId(UserDetails userDetails) {
        return userMapper.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND))
                .getUserId();
    }

    private Teacher getTeacher(Long userId) {
        return teacherMapper.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEACHER_NOT_FOUND));
    }
}
