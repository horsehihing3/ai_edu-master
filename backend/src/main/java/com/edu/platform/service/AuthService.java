package com.edu.platform.service;

import com.edu.platform.config.JwtProvider;
import com.edu.platform.domain.PasswordResetToken;
import com.edu.platform.domain.School;
import com.edu.platform.domain.Student;
import com.edu.platform.domain.Teacher;
import com.edu.platform.domain.User;
import com.edu.platform.dto.auth.LoginRequest;
import com.edu.platform.dto.auth.LoginResponse;
import com.edu.platform.dto.auth.RegisterRequest;
import com.edu.platform.dto.auth.TokenRefreshRequest;
import com.edu.platform.dto.common.UserInfo;
import com.edu.platform.exception.BusinessException;
import com.edu.platform.exception.ErrorCode;
import com.edu.platform.domain.EmailVerification;
import com.edu.platform.mapper.EmailVerificationMapper;
import com.edu.platform.mapper.PasswordResetTokenMapper;
import com.edu.platform.mapper.SchoolMapper;
import com.edu.platform.mapper.StudentMapper;
import com.edu.platform.mapper.TeacherMapper;
import com.edu.platform.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final StudentMapper studentMapper;
    private final TeacherMapper teacherMapper;
    private final SchoolMapper schoolMapper;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final PasswordResetTokenMapper passwordResetTokenMapper;
    private final EmailVerificationMapper emailVerificationMapper;
    private final EmailService emailService;

    @Value("${app.mail.reset-token-expiry-minutes:30}")
    private int resetTokenExpiryMinutes;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        // [2026-04-01] 탈퇴 계정 로그인 차단 — authenticate() 전에 체크해야 500 방지
        User user = userMapper.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (user.getIsActive() != null && !user.getIsActive()) {
            throw new BusinessException(ErrorCode.USER_INACTIVE);
        }
        // [2026-04-04] 이메일 미인증 계정 로그인 차단
        if (user.getIsEmailVerified() != null && !user.getIsEmailVerified()) {
            throw new BusinessException(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        userMapper.updateLastLogin(user.getUserId());

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        String accessToken = jwtProvider.generateAccessToken(userDetails);
        String refreshToken = jwtProvider.generateRefreshToken(request.getEmail());

        String schoolName = null;
        String studentLevel = null;

        if (user.getSchoolId() != null) {
            schoolName = schoolMapper.findById(user.getSchoolId())
                    .map(School::getSchoolName)
                    .orElse(null);
        }

        if ("STUDENT".equals(user.getRole())) {
            studentLevel = studentMapper.findByUserId(user.getUserId())
                    .map(Student::getStudentLevel)
                    .orElse(null);
        }

        UserInfo userInfo = UserInfo.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRoleAsEnum())
                .schoolName(schoolName)
                .studentLevel(studentLevel)
                .profileImgUrl(user.getProfileImgUrl())
                .build();

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(3600000L)
                .userInfo(userInfo)
                .build();
    }

    @Transactional
    public void register(RegisterRequest request) {
        if (userMapper.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        Long schoolId = null;
        if (request.getSchoolCode() != null) {
            School school = schoolMapper.findByCode(request.getSchoolCode())
                    .orElseThrow(() -> new BusinessException(ErrorCode.SCHOOL_CODE_NOT_FOUND));
            schoolId = school.getSchoolId();
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .role(request.getRole() != null ? request.getRole().name() : null)
                .schoolId(schoolId)
                .phone(request.getPhone())
                .isActive(true)
                .isEmailVerified(false)
                .build();

        userMapper.insert(user);

        if (request.getRole() == User.UserRole.STUDENT && schoolId != null) {
            Student student = Student.builder()
                    .userId(user.getUserId())
                    .schoolId(schoolId)
                    .grade(request.getGrade() != null ? request.getGrade().name() : null)
                    .build();
            studentMapper.insert(student);
        } else if (request.getRole() == User.UserRole.TEACHER && schoolId != null) {
            Teacher teacher = Teacher.builder()
                    .userId(user.getUserId())
                    .schoolId(schoolId)
                    .build();
            teacherMapper.insert(teacher);
        }

        log.info("New user registered: {}", request.getEmail());

        // [2026-04-04] 회원가입 후 이메일 인증 토큰 자동 발송 — 발송 실패해도 가입은 유지
        try {
            sendVerificationEmail(request.getEmail());
        } catch (Exception e) {
            log.warn("Verification email failed for {}: {}", request.getEmail(), e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public UserInfo getMe(String email) {
        User user = userMapper.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String schoolName = null;
        String studentLevel = null;

        if (user.getSchoolId() != null) {
            schoolName = schoolMapper.findById(user.getSchoolId())
                    .map(School::getSchoolName)
                    .orElse(null);
        }

        if ("STUDENT".equals(user.getRole())) {
            studentLevel = studentMapper.findByUserId(user.getUserId())
                    .map(Student::getStudentLevel)
                    .orElse(null);
        }

        return UserInfo.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRoleAsEnum())
                .schoolName(schoolName)
                .studentLevel(studentLevel)
                .profileImgUrl(user.getProfileImgUrl())
                .build();
    }

    public LoginResponse refreshToken(TokenRefreshRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }

        String tokenType = jwtProvider.getTokenType(refreshToken);
        if (!"refresh".equals(tokenType)) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }

        String email = jwtProvider.getEmailFromToken(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        User user = userMapper.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String newAccessToken = jwtProvider.generateAccessToken(userDetails);
        String newRefreshToken = jwtProvider.generateRefreshToken(email);

        UserInfo userInfo = UserInfo.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRoleAsEnum())
                .build();

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(3600000L)
                .userInfo(userInfo)
                .build();
    }

    @Transactional
    public void sendPasswordResetEmail(String email) {
        // 열거 공격 방지 — 사용자 없어도 동일 응답
        userMapper.findByEmail(email).ifPresent(user -> {
            passwordResetTokenMapper.deleteExpiredByUserId(user.getUserId());

            String token = UUID.randomUUID().toString().replace("-", "");
            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setUserId(user.getUserId());
            resetToken.setToken(token);
            resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(resetTokenExpiryMinutes));
            resetToken.setUsed(false);
            passwordResetTokenMapper.insert(resetToken);

            emailService.sendPasswordResetEmail(user.getEmail(), token, user.getName());
        });
        log.info("Password reset requested for: {}", email);
    }

    @Transactional(readOnly = true)
    public void validateResetToken(String token) {
        PasswordResetToken resetToken = passwordResetTokenMapper.findByToken(token);
        if (resetToken == null) {
            throw new BusinessException(ErrorCode.RESET_TOKEN_INVALID);
        }
        if (resetToken.isUsed()) {
            throw new BusinessException(ErrorCode.RESET_TOKEN_ALREADY_USED);
        }
        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.RESET_TOKEN_EXPIRED);
        }
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenMapper.findByToken(token);
        if (resetToken == null) {
            throw new BusinessException(ErrorCode.RESET_TOKEN_INVALID);
        }
        if (resetToken.isUsed()) {
            throw new BusinessException(ErrorCode.RESET_TOKEN_ALREADY_USED);
        }
        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.RESET_TOKEN_EXPIRED);
        }

        String encodedPassword = passwordEncoder.encode(newPassword);
        userMapper.updatePassword(resetToken.getUserId(), encodedPassword);
        passwordResetTokenMapper.markUsed(token);
        log.info("Password reset completed for userId: {}", resetToken.getUserId());
    }

    // [2026-04-04] 이메일 인증 토큰 생성 및 발송
    @Transactional
    public void sendVerificationEmail(String email) {
        User user = userMapper.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (Boolean.TRUE.equals(user.getIsEmailVerified())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "이미 인증된 이메일입니다.");
        }

        // 기존 미사용 토큰 삭제 후 새 토큰 생성
        emailVerificationMapper.deleteUnusedByEmail(email);

        String token = UUID.randomUUID().toString();
        EmailVerification ev = new EmailVerification();
        ev.setUserId(user.getUserId());
        ev.setEmail(email);
        ev.setToken(token);
        ev.setPurpose("SIGNUP");
        ev.setExpiresAt(LocalDateTime.now().plusHours(24));
        emailVerificationMapper.insert(ev);

        emailService.sendVerificationEmail(email, token, user.getName());
        log.info("Verification email sent to: {}", email);
    }

    // [2026-04-04] 토큰 검증 후 이메일 인증 처리
    @Transactional
    public void verifyEmail(String token) {
        EmailVerification ev = emailVerificationMapper.findByToken(token);
        if (ev == null) {
            throw new BusinessException(ErrorCode.VERIFICATION_TOKEN_INVALID);
        }
        if (ev.getUsedAt() != null) {
            throw new BusinessException(ErrorCode.VERIFICATION_TOKEN_INVALID);
        }
        if (ev.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.VERIFICATION_TOKEN_EXPIRED);
        }

        userMapper.setEmailVerified(ev.getUserId());
        emailVerificationMapper.markUsed(token);
        log.info("Email verified for userId: {}", ev.getUserId());
    }

    public void logout(String userId) {
        // JWT stateless 방식이므로 클라이언트 토큰 삭제로 처리
        // Redis 등을 사용한 블랙리스트 방식 구현 가능
        log.info("User logged out: {}", userId);
    }
}
