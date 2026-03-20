package com.edu.platform.controller;

import com.edu.platform.domain.User;
import com.edu.platform.dto.common.ApiResponse;
import com.edu.platform.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public")
@RequiredArgsConstructor
public class SetupController {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/setup-admin")
    public ResponseEntity<ApiResponse<String>> setupAdmin() {
        String email = "admin@edu-platform.com";

        if (userMapper.findByEmail(email).isPresent()) {
            // 이미 존재하면 비밀번호만 재설정
            User user = userMapper.findByEmail(email).get();
            userMapper.updatePassword(user.getUserId(), passwordEncoder.encode("Admin1234"));
            return ResponseEntity.ok(ApiResponse.success("관리자 비밀번호가 재설정되었습니다. 비밀번호: Admin1234"));
        }

        User admin = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode("Admin1234"))
                .name("관리자")
                .role("ADMIN")
                .isActive(true)
                .isEmailVerified(true)
                .build();

        userMapper.insert(admin);
        return ResponseEntity.ok(ApiResponse.success("관리자 계정이 생성되었습니다. 이메일: " + email + " / 비밀번호: Admin1234"));
    }
}
