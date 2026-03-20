package com.edu.platform.dto.common;

import com.edu.platform.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {

    private Long userId;
    private String email;
    private String name;
    private User.UserRole role;
    private String schoolName;
    private String studentLevel;
    private String profileImgUrl;
}
