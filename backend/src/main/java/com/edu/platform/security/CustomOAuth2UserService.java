package com.edu.platform.security;

import com.edu.platform.domain.Student;
import com.edu.platform.domain.User;
import com.edu.platform.mapper.StudentMapper;
import com.edu.platform.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * [2026-04-04] OAuth2 제공자(Kakao/Google)에서 받은 사용자 정보를 우리 DB User로 매핑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserMapper userMapper;
    private final StudentMapper studentMapper;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String nameAttributeKey = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email;
        String name;

        if ("google".equals(registrationId)) {
            email = (String) attributes.get("email");
            name  = (String) attributes.get("name");
        } else if ("kakao".equals(registrationId)) {
            @SuppressWarnings("unchecked")
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            @SuppressWarnings("unchecked")
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            email = (String) kakaoAccount.get("email");
            name  = (String) profile.get("nickname");
        } else {
            throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인 제공자: " + registrationId);
        }

        if (email == null) {
            throw new OAuth2AuthenticationException("소셜 계정에서 이메일 정보를 가져올 수 없습니다.");
        }

        // 기존 회원이면 로그인, 신규면 STUDENT로 자동 가입
        final String finalEmail = email;
        final String finalName  = name;
        User user = userMapper.findByEmail(finalEmail).orElseGet(() -> {
            User newUser = User.builder()
                    .email(finalEmail)
                    .name(finalName != null ? finalName : finalEmail)
                    .role("STUDENT")
                    .isActive(true)
                    .isEmailVerified(true)   // 소셜 로그인은 이메일 인증 완료로 간주
                    .build();
            userMapper.insert(newUser);

            Student student = Student.builder()
                    .userId(newUser.getUserId())
                    .build();
            studentMapper.insert(student);

            log.info("OAuth2 신규 가입: {} ({})", finalEmail, registrationId);
            return newUser;
        });

        return new OAuth2UserPrincipal(user, attributes, nameAttributeKey);
    }
}
