package com.edu.platform.controller;

import com.edu.platform.common.ResponseMessage;
import com.edu.platform.dto.auth.LoginRequest;
import com.edu.platform.dto.auth.LoginResponse;
import com.edu.platform.dto.auth.PasswordResetDto;
import com.edu.platform.dto.auth.PasswordResetRequestDto;
import com.edu.platform.dto.auth.RegisterRequest;
import com.edu.platform.dto.auth.TokenRefreshRequest;
import com.edu.platform.dto.common.ApiResponse;
import com.edu.platform.dto.common.UserInfo;
import com.edu.platform.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.LOGIN_SUCCESS, response));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(ResponseMessage.REGISTER_SUCCESS));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(
            @Valid @RequestBody TokenRefreshRequest request) {
        LoginResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.TOKEN_REFRESHED, response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String authorization) {
        authService.logout(authorization);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.LOGOUT_SUCCESS));
    }

    @PostMapping("/password/forgot")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody PasswordResetRequestDto request) {
        authService.sendPasswordResetEmail(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.PASSWORD_RESET_EMAIL_SENT));
    }

    @GetMapping("/password/validate-token")
    public ResponseEntity<ApiResponse<Void>> validateToken(@RequestParam String token) {
        authService.validateResetToken(token);
        return ResponseEntity.ok(ApiResponse.success("유효한 토큰입니다."));
    }

    @PostMapping("/password/reset")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody PasswordResetDto request) {
        authService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.PASSWORD_CHANGED));
    }

    @PostMapping("/email/verify")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            @RequestBody Map<String, String> request) {
        authService.verifyEmail(request.get("token"));
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.EMAIL_VERIFIED));
    }

    @PostMapping("/email/resend")
    public ResponseEntity<ApiResponse<Void>> resendVerificationEmail(
            @RequestBody Map<String, String> request) {
        authService.sendVerificationEmail(request.get("email"));
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.EMAIL_RESENT));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfo>> getMe(@AuthenticationPrincipal UserDetails userDetails) {
        UserInfo userInfo = authService.getMe(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(userInfo));
    }

    @GetMapping("/oauth2/login/{provider}")
    public ResponseEntity<ApiResponse<Map<String, String>>> oauth2Login(@PathVariable String provider) {
        // OAuth2 로그인 URL 반환 (실제 구현은 Spring Security OAuth2가 처리)
        return ResponseEntity.ok(ApiResponse.success(Map.of("provider", provider)));
    }
}
