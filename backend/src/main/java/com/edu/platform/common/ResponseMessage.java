package com.edu.platform.common;

public final class ResponseMessage {
    private ResponseMessage() {}

    // ── Auth ──────────────────────────────────────────────
    public static final String LOGIN_SUCCESS             = "로그인되었습니다.";
    public static final String LOGOUT_SUCCESS            = "로그아웃되었습니다.";
    public static final String REGISTER_SUCCESS          = "회원가입이 완료되었습니다.";
    public static final String TOKEN_REFRESHED           = "토큰이 갱신되었습니다.";
    public static final String PASSWORD_RESET_EMAIL_SENT = "비밀번호 재설정 이메일을 전송했습니다.";
    public static final String PASSWORD_CHANGED          = "비밀번호가 변경되었습니다.";
    public static final String EMAIL_VERIFIED            = "이메일 인증이 완료되었습니다.";
    public static final String EMAIL_RESENT              = "인증 이메일을 재전송했습니다.";

    // ── User / Admin ───────────────────────────────────────
    public static final String USER_STATUS_UPDATED       = "사용자 상태가 변경되었습니다.";
    public static final String USER_PASSWORD_RESET       = "비밀번호가 초기화되었습니다.";
    public static final String PROFILE_UPDATED           = "프로필이 수정되었습니다.";
    public static final String NOTIFICATION_SETTINGS_SAVED = "알림 설정이 저장되었습니다.";
    public static final String ACCOUNT_DELETED           = "계정이 탈퇴 처리되었습니다.";

    // ── School ────────────────────────────────────────────
    public static final String SCHOOL_CREATED            = "학교가 등록되었습니다.";
    public static final String SCHOOL_UPDATED            = "학교 정보가 수정되었습니다.";

    // ── Problem ───────────────────────────────────────────
    public static final String PROBLEM_CREATED           = "문제가 등록되었습니다.";
    public static final String PROBLEM_UPDATED           = "문제가 수정되었습니다.";
    public static final String PROBLEM_DELETED           = "문제가 삭제되었습니다.";

    // ── Assignment ────────────────────────────────────────
    public static final String ASSIGNMENT_CREATED        = "과제가 생성되었습니다.";
    public static final String ASSIGNMENT_UPDATED        = "과제가 수정되었습니다.";
    public static final String ASSIGNMENT_DELETED        = "과제가 삭제되었습니다.";

    // ── Session ───────────────────────────────────────────
    public static final String SESSION_STARTED           = "학습 세션이 시작되었습니다.";
    public static final String SESSION_SAVED             = "진행 상황이 저장되었습니다.";
    public static final String SESSION_COMPLETED         = "학습 세션이 완료되었습니다.";
    public static final String ANSWER_SUBMITTED          = "답안이 제출되었습니다.";

    // ── Bookmark ──────────────────────────────────────────
    public static final String BOOKMARK_ADDED            = "북마크가 추가되었습니다.";
    public static final String BOOKMARK_DELETED          = "북마크가 삭제되었습니다.";

    // ── Announcement ─────────────────────────────────────
    public static final String ANNOUNCEMENT_CREATED      = "공지사항이 등록되었습니다.";
    public static final String ANNOUNCEMENT_UPDATED      = "공지사항이 수정되었습니다.";
    public static final String ANNOUNCEMENT_DELETED      = "공지사항이 삭제되었습니다.";

    // ── Inquiry ───────────────────────────────────────────
    public static final String INQUIRY_CREATED           = "문의가 등록되었습니다.";
    public static final String INQUIRY_REPLY_CREATED     = "답변이 등록되었습니다.";

    // ── Video ─────────────────────────────────────────────
    public static final String VIDEO_REQUEST_ACCEPTED    = "동영상 풀이 요청이 접수되었습니다.";
    public static final String VIDEO_UPLOADED            = "동영상이 업로드되었습니다.";
    public static final String VIDEO_DELETED             = "동영상이 삭제되었습니다.";

    // ── Notification ──────────────────────────────────────
    public static final String NOTIFICATION_READ         = "알림이 읽음 처리되었습니다.";
    public static final String NOTIFICATION_ALL_READ     = "모든 알림이 읽음 처리되었습니다.";

    // ── Diagnosis ─────────────────────────────────────────
    public static final String DIAGNOSIS_STARTED         = "진단 테스트가 시작되었습니다.";
    public static final String DIAGNOSIS_SUBMITTED       = "답안이 제출되었습니다.";
    public static final String DIAGNOSIS_COMPLETED       = "진단 테스트가 완료되었습니다.";

    // ── System Code ───────────────────────────────────────
    public static final String CODE_CREATED              = "코드가 등록되었습니다.";
    public static final String CODE_UPDATED              = "코드가 수정되었습니다.";
    public static final String CODE_DELETED              = "코드가 삭제되었습니다.";
}
