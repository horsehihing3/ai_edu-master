package com.edu.platform.service;

import com.edu.platform.domain.SchoolClass;
import com.edu.platform.dto.teacher.ClassDto;
import com.edu.platform.exception.BusinessException;
import com.edu.platform.exception.ErrorCode;
import com.edu.platform.mapper.ClassMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassService {

    private final ClassMapper classMapper;

    public List<ClassDto.ClassResponse> getClassesByTeacher(Long teacherId) {
        return classMapper.findByTeacherId(teacherId).stream()
                .map(c -> ClassDto.ClassResponse.builder()
                        .classId(c.getClassId())
                        .className(c.getClassName())
                        .grade(c.getGrade())
                        .levelFilter(c.getLevelFilter())
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
                .build();
        classMapper.insert(schoolClass);
        return ClassDto.ClassResponse.builder()
                .classId(schoolClass.getClassId())
                .className(schoolClass.getClassName())
                .grade(schoolClass.getGrade())
                .levelFilter(schoolClass.getLevelFilter())
                .studentCount(0)
                .createdAt(schoolClass.getCreatedAt())
                .build();
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

    private SchoolClass getClassOwnedByTeacher(Long classId, Long teacherId) {
        SchoolClass schoolClass = classMapper.findById(classId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST));
        if (!schoolClass.getTeacherId().equals(teacherId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        return schoolClass;
    }
}
