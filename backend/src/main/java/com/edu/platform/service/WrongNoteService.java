package com.edu.platform.service;

import com.edu.platform.domain.WrongNote;
import com.edu.platform.dto.common.PageResponse;
import com.edu.platform.dto.student.WrongNoteDto;
import com.edu.platform.exception.BusinessException;
import com.edu.platform.exception.ErrorCode;
import com.edu.platform.mapper.ProblemMapper;
import com.edu.platform.mapper.WrongNoteMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WrongNoteService {

    private final WrongNoteMapper wrongNoteMapper;
    private final ProblemMapper problemMapper;

    @Transactional
    public void autoSave(Long studentId, Long problemId, Long attemptId) {
        WrongNote wrongNote = WrongNote.builder()
                .studentId(studentId)
                .problemId(problemId)
                .attemptId(attemptId)
                .build();
        wrongNoteMapper.insertOrUpdate(wrongNote);
    }

    @Transactional(readOnly = true)
    public PageResponse<WrongNoteDto> getWrongNotes(Long studentId, int page, int size) {
        int offset = page * size;

        List<WrongNote> wrongNotes = wrongNoteMapper.findByStudentId(studentId, offset, size);
        long totalElements = wrongNoteMapper.countByStudentId(studentId);
        int totalPages = (int) Math.ceil((double) totalElements / size);

        List<WrongNoteDto> content = wrongNotes.stream()
                .map(wn -> {
                    WrongNoteDto.WrongNoteDtoBuilder builder = WrongNoteDto.builder()
                            .wrongNoteId(wn.getWrongNoteId())
                            .problemId(wn.getProblemId())
                            .memo(wn.getMemo() != null ? wn.getMemo() : "")
                            .isResolved(wn.getIsResolved() != null ? wn.getIsResolved() : false)
                            .createdAt(wn.getCreatedAt() != null ? wn.getCreatedAt().toString() : "");

                    problemMapper.findById(wn.getProblemId()).ifPresent(p -> {
                        builder.subject(p.getSubject() != null ? p.getSubject() : "");
                        builder.level(p.getLevel() != null ? p.getLevel() : "");
                        builder.unitName(p.getUnitName() != null ? p.getUnitName() : "");
                        builder.questionText(p.getQuestionText() != null ? p.getQuestionText() : "");
                    });

                    return builder.build();
                })
                .collect(Collectors.toList());

        return PageResponse.of(content, totalElements, totalPages, page, size);
    }

    @Transactional
    public void deleteWrongNote(Long studentId, Long wrongNoteId) {
        wrongNoteMapper.findById(wrongNoteId, studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WRONG_NOTE_NOT_FOUND));
        wrongNoteMapper.deleteById(wrongNoteId, studentId);
    }

    @Transactional
    public void deleteAllWrongNotes(Long studentId) {
        wrongNoteMapper.deleteAllByStudentId(studentId);
    }

    @Transactional
    public void toggleResolved(Long studentId, Long wrongNoteId) {
        WrongNote wrongNote = wrongNoteMapper.findById(wrongNoteId, studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WRONG_NOTE_NOT_FOUND));
        boolean newResolved = wrongNote.getIsResolved() == null || !wrongNote.getIsResolved();
        wrongNoteMapper.updateResolved(wrongNoteId, studentId, newResolved);
    }
}
