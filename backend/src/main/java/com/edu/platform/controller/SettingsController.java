package com.edu.platform.controller;

import com.edu.platform.common.ResponseMessage;
import com.edu.platform.domain.User;
import com.edu.platform.dto.common.ApiResponse;
import com.edu.platform.exception.BusinessException;
import com.edu.platform.exception.ErrorCode;
import com.edu.platform.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        Map<String, Object> profile = Map.of(
                "userId", user.getUserId(),
                "email", user.getEmail(),
                "name", user.getName(),
                "phone", user.getPhone() != null ? user.getPhone() : "",
                "profileImgUrl", user.getProfileImgUrl() != null ? user.getProfileImgUrl() : "",
                "role", user.getRole() != null ? user.getRole() : ""
        );
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @PutMapping("/profile")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> request) {
        User user = getUser(userDetails);
        if (request.containsKey("name")) {
            user.setName(request.get("name"));
        }
        if (request.containsKey("phone")) {
            user.setPhone(request.get("phone"));
        }
        if (request.containsKey("profileImgUrl")) {
            user.setProfileImgUrl(request.get("profileImgUrl"));
        }
        userMapper.update(user);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.PROFILE_UPDATED));
    }

    @PutMapping("/password")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> updatePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> request) {
        User user = getUser(userDetails);
        String currentPassword = request.get("currentPassword");
        String newPassword = request.get("newPassword");

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        userMapper.updatePassword(user.getUserId(), passwordEncoder.encode(newPassword));
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.PASSWORD_CHANGED));
    }

    @PutMapping("/notifications")
    public ResponseEntity<ApiResponse<Void>> updateNotificationSettings(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Object> request) {
        // TODO: 알림 설정 저장 로직 구현
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.NOTIFICATION_SETTINGS_SAVED));
    }

    @DeleteMapping("/account")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> deleteAccount(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        userMapper.updateActive(user.getUserId(), false);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.ACCOUNT_DELETED));
    }

    private User getUser(UserDetails userDetails) {
        return userMapper.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
