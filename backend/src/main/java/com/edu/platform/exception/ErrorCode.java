package com.edu.platform.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // 사용자 관련
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 올바르지 않습니다."),
    USER_INACTIVE(HttpStatus.FORBIDDEN, "비활성화된 계정입니다."),
    EMAIL_NOT_VERIFIED(HttpStatus.FORBIDDEN, "이메일 인증이 필요합니다."),

    // 인증/인가 관련
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "토큰을 찾을 수 없습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),

    // 학교 관련
    SCHOOL_NOT_FOUND(HttpStatus.NOT_FOUND, "학교를 찾을 수 없습니다."),
    SCHOOL_CODE_NOT_FOUND(HttpStatus.NOT_FOUND, "유효하지 않은 학교 코드입니다."),
    SCHOOL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 학교 코드입니다."),

    // 학생 관련
    STUDENT_NOT_FOUND(HttpStatus.NOT_FOUND, "학생 정보를 찾을 수 없습니다."),
    DIAGNOSIS_REQUIRED(HttpStatus.BAD_REQUEST, "진단 테스트를 먼저 완료해주세요."),
    DIAGNOSIS_ALREADY_IN_PROGRESS(HttpStatus.BAD_REQUEST, "진단 테스트가 이미 진행 중입니다."),
    DIAGNOSIS_NOT_FOUND(HttpStatus.NOT_FOUND, "진단 테스트를 찾을 수 없습니다."),

    // 선생님 관련
    TEACHER_NOT_FOUND(HttpStatus.NOT_FOUND, "선생님 정보를 찾을 수 없습니다."),

    // 과제 관련
    ASSIGNMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "과제를 찾을 수 없습니다."),
    ASSIGNMENT_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "이미 완료된 과제입니다."),

    // 학습 세션 관련
    SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "학습 세션을 찾을 수 없습니다."),
    SESSION_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "이미 완료된 세션입니다."),
    SESSION_NOT_IN_PROGRESS(HttpStatus.BAD_REQUEST, "진행 중이지 않은 세션입니다."),

    // 문제 관련
    PROBLEM_NOT_FOUND(HttpStatus.NOT_FOUND, "문제를 찾을 수 없습니다."),
    PROBLEM_ALREADY_SUBMITTED(HttpStatus.BAD_REQUEST, "이미 제출한 문제입니다."),

    // 북마크 관련
    BOOKMARK_NOT_FOUND(HttpStatus.NOT_FOUND, "북마크를 찾을 수 없습니다."),
    BOOKMARK_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 북마크된 문제입니다."),

    // 동영상 관련
    VIDEO_NOT_FOUND(HttpStatus.NOT_FOUND, "동영상을 찾을 수 없습니다."),

    // 알림 관련
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "알림을 찾을 수 없습니다."),

    // 문의 관련
    INQUIRY_NOT_FOUND(HttpStatus.NOT_FOUND, "문의를 찾을 수 없습니다."),

    // 공지사항 관련
    ANNOUNCEMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "공지사항을 찾을 수 없습니다."),

    // 파일 관련
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다."),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "파일을 찾을 수 없습니다."),

    // 이메일 관련
    EMAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이메일 전송에 실패했습니다."),
    VERIFICATION_TOKEN_INVALID(HttpStatus.BAD_REQUEST, "유효하지 않은 인증 토큰입니다."),
    VERIFICATION_TOKEN_EXPIRED(HttpStatus.BAD_REQUEST, "만료된 인증 토큰입니다."),

    // 시스템 코드 관련
    CODE_NOT_FOUND(HttpStatus.NOT_FOUND, "코드를 찾을 수 없습니다."),
    CODE_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 코드입니다."),
    CODE_HAS_CHILDREN(HttpStatus.BAD_REQUEST, "하위 코드가 존재하여 삭제할 수 없습니다."),

    // 공통
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
