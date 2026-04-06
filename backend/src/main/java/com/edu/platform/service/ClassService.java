package com.edu.platform.service;

import com.edu.platform.domain.SchoolClass;
import com.edu.platform.dto.teacher.ClassDto;
import com.edu.platform.exception.BusinessException;
import com.edu.platform.exception.ErrorCode;
import com.edu.platform.mapper.ClassMapper;
import com.edu.platform.mapper.StudentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassService {

    private static final String INVITE_CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // 혼동 문자(0,1,I,O) 제외
    private static final int INVITE_CODE_LENGTH = 8;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ClassMapper classMapper;
    private final StudentMapper studentMapper;

    public List<ClassDto.ClassResponse> getClassesByTeacher(Long teacherId) {
        return classMapper.findByTeacherId(teacherId).stream()
                .map(c -> ClassDto.ClassResponse.builder()
                        .classId(c.getClassId())
                        .className(c.getClassName())
                        .grade(c.getGrade())
                        .levelFilter(c.getLevelFilter())
                        .inviteCode(c.getInviteCode())
                        .studentCount(classMapper.countMembers(c.getClassId()))
                        .createdAt(c.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public ClassDto.ClassResponse createClass(Long teacherId, Long schoolId, ClassDto.ClassCreateRequest req) {
        SchoolClass schoolClass = SchoolClass.builder()
                .teacherId(teacherId)
                .schoolId(schoolId)
                .className(req.getClassName())
                .grade(req.getGrade())
                .levelFilter(req.getLevelFilter() != null ? req.getLevelFilter() : "ALL")
                .inviteCode(generateUniqueInviteCode())
                .build();
        classMapper.insert(schoolClass);
        return ClassDto.ClassResponse.builder()
                .classId(schoolClass.getClassId())
                .className(schoolClass.getClassName())
                .grade(schoolClass.getGrade())
                .levelFilter(schoolClass.getLevelFilter())
                .inviteCode(schoolClass.getInviteCode())
                .studentCount(0)
                .createdAt(schoolClass.getCreatedAt())
                .build();
    }

    // [2026-04-03] 학생이 초대코드로 학급 가입
    @Transactional
    public Map<String, Object> joinByCode(Long userId, String inviteCode) {
        Long studentId = studentMapper.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND))
                .getStudentId();

        SchoolClass schoolClass = classMapper.findByInviteCode(inviteCode.toUpperCase())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INVITE_CODE));

        if (classMapper.countMemberByStudentId(schoolClass.getClassId(), studentId) > 0) {
            throw new BusinessException(ErrorCode.ALREADY_CLASS_MEMBER);
        }

        classMapper.addMember(schoolClass.getClassId(), studentId);
        return Map.of(
                "classId", schoolClass.getClassId(),
                "className", schoolClass.getClassName()
        );
    }

    public List<Map<String, Object>> getMyClasses(Long studentId) {
        return classMapper.findClassesByStudentId(studentId);
    }

    private String generateUniqueInviteCode() {
        for (int attempt = 0; attempt < 10; attempt++) {
            StringBuilder sb = new StringBuilder(INVITE_CODE_LENGTH);
            for (int i = 0; i < INVITE_CODE_LENGTH; i++) {
                sb.append(INVITE_CODE_CHARS.charAt(RANDOM.nextInt(INVITE_CODE_CHARS.length())));
            }
            String code = sb.toString();
            if (classMapper.findByInviteCode(code).isEmpty()) {
                return code;
            }
        }
        throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    @Transactional
    public ClassDto.ClassResponse updateClass(Long teacherId, Long classId, ClassDto.ClassUpdateRequest req) {
        SchoolClass schoolClass = getClassOwnedByTeacher(classId, teacherId);
        if (req.getClassName() != null) schoolClass.setClassName(req.getClassName());
        if (req.getGrade() != null) schoolClass.setGrade(req.getGrade());
        if (req.getLevelFilter() != null) schoolClass.setLevelFilter(req.getLevelFilter());
        classMapper.update(schoolClass);
        return ClassDto.ClassResponse.builder()
                .classId(schoolClass.getClassId())
                .className(schoolClass.getClassName())
                .grade(schoolClass.getGrade())
                .levelFilter(schoolClass.getLevelFilter())
                .studentCount(classMapper.countMembers(classId))
                .createdAt(schoolClass.getCreatedAt())
                .build();
    }

    @Transactional
    public void deleteClass(Long teacherId, Long classId) {
        getClassOwnedByTeacher(classId, teacherId);
        classMapper.deleteById(classId);
    }

    public List<Map<String, Object>> getMembers(Long teacherId, Long classId) {
        getClassOwnedByTeacher(classId, teacherId);
        return classMapper.findMembersByClassId(classId);
    }

    @Transactional
    public void addMembers(Long teacherId, Long classId, List<Long> studentIds) {
        getClassOwnedByTeacher(classId, teacherId);
        for (Long studentId : studentIds) {
            classMapper.addMember(classId, studentId);
        }
    }

    @Transactional
    public void removeMember(Long teacherId, Long classId, Long studentId) {
        getClassOwnedByTeacher(classId, teacherId);
        classMapper.removeMember(classId, studentId);
    }

    // [2026-04-06] 교사가 학생을 본인의 다른 학급으로 이동
    @Transactional
    public void moveStudent(Long teacherId, Long fromClassId, Long studentId, Long targetClassId) {
        getClassOwnedByTeacher(fromClassId, teacherId);
        getClassOwnedByTeacher(targetClassId, teacherId);
        classMapper.removeMember(fromClassId, studentId);
        classMapper.addMember(targetClassId, studentId);
    }

    // [2026-04-06] 어드민용 전체 활성 학급 목록
    public List<Map<String, Object>> getAllActiveClasses() {
        return classMapper.findAllActive();
    }

    // [2026-04-06] 어드민이 학생을 임의 학급으로 재배정
    @Transactional
    public void reassignStudent(Long studentId, Long fromClassId, Long toClassId) {
        classMapper.removeMember(fromClassId, studentId);
        classMapper.addMember(toClassId, studentId);
    }

    private SchoolClass getClassOwnedByTeacher(Long classId, Long teacherId) {
        SchoolClass schoolClass = classMapper.findById(classId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST));
        if (!schoolClass.getTeacherId().equals(teacherId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        return schoolClass;
    }
}
