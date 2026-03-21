package com.edu.platform.controller;

import com.edu.platform.common.ResponseMessage;
import com.edu.platform.domain.Teacher;
import com.edu.platform.dto.common.ApiResponse;
import com.edu.platform.dto.teacher.ClassDto;
import com.edu.platform.exception.BusinessException;
import com.edu.platform.exception.ErrorCode;
import com.edu.platform.mapper.TeacherMapper;
import com.edu.platform.mapper.UserMapper;
import com.edu.platform.service.ClassService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/teacher/classes")
@RequiredArgsConstructor
public class ClassController {

    private final ClassService classService;
    private final UserMapper userMapper;
    private final TeacherMapper teacherMapper;

    // GET /teacher/classes
    @GetMapping
    public ResponseEntity<ApiResponse<List<ClassDto.ClassResponse>>> getClasses(
            @AuthenticationPrincipal UserDetails userDetails) {
        Teacher teacher = getTeacher(userDetails);
        return ResponseEntity.ok(ApiResponse.success(
                classService.getClassesByTeacher(teacher.getTeacherId())));
    }

    // POST /teacher/classes
    @PostMapping
    public ResponseEntity<ApiResponse<ClassDto.ClassResponse>> createClass(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ClassDto.ClassCreateRequest request) {
        Teacher teacher = getTeacher(userDetails);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.CLASS_CREATED,
                classService.createClass(teacher.getTeacherId(), teacher.getSchoolId(), request)));
    }

    // PUT /teacher/classes/{classId}
    @PutMapping("/{classId}")
    public ResponseEntity<ApiResponse<ClassDto.ClassResponse>> updateClass(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long classId,
            @RequestBody ClassDto.ClassUpdateRequest request) {
        Teacher teacher = getTeacher(userDetails);
        return ResponseEntity.ok(ApiResponse.success(
                classService.updateClass(teacher.getTeacherId(), classId, request)));
    }

    // DELETE /teacher/classes/{classId}
    @DeleteMapping("/{classId}")
    public ResponseEntity<ApiResponse<Void>> deleteClass(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long classId) {
        Teacher teacher = getTeacher(userDetails);
        classService.deleteClass(teacher.getTeacherId(), classId);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.CLASS_DELETED));
    }

    // GET /teacher/classes/{classId}/members
    @GetMapping("/{classId}/members")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getMembers(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long classId) {
        Teacher teacher = getTeacher(userDetails);
        return ResponseEntity.ok(ApiResponse.success(
                classService.getMembers(teacher.getTeacherId(), classId)));
    }

    // POST /teacher/classes/{classId}/members
    @PostMapping("/{classId}/members")
    public ResponseEntity<ApiResponse<Void>> addMembers(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long classId,
            @RequestBody ClassDto.AddMembersRequest request) {
        Teacher teacher = getTeacher(userDetails);
        classService.addMembers(teacher.getTeacherId(), classId, request.getStudentIds());
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.CLASS_MEMBER_ADDED));
    }

    // DELETE /teacher/classes/{classId}/members/{studentId}
    @DeleteMapping("/{classId}/members/{studentId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long classId,
            @PathVariable Long studentId) {
        Teacher teacher = getTeacher(userDetails);
        classService.removeMember(teacher.getTeacherId(), classId, studentId);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.CLASS_MEMBER_REMOVED));
    }

    private Teacher getTeacher(UserDetails userDetails) {
        Long userId = userMapper.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND))
                .getUserId();
        return teacherMapper.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEACHER_NOT_FOUND));
    }
}
