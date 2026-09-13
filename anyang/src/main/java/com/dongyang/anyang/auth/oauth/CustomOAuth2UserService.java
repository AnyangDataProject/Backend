package com.dongyang.anyang.auth.oauth;

import com.dongyang.anyang.domain.user.Provider;
import com.dongyang.anyang.domain.user.Role;
import com.dongyang.anyang.domain.user.User;
import com.dongyang.anyang.domain.user.UserRepository;
import com.dongyang.anyang.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("#{'${admin.emails}'.split(',\\s*')}")
    private List<String> adminEmails;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo userInfo = getOAuth2UserInfo(registrationId, oAuth2User.getAttributes());

        User user = userRepository.findByEmail(userInfo.getEmail())
                .orElseGet(() -> registerNewUser(userInfo, registrationId));

        Role syncedRole = resolveRole(user.getEmail());
        if(syncedRole != user.getRole()) {
            user.updateRole(syncedRole);
        }

        return new CustomUserDetails(user, oAuth2User.getAttributes());
    }

    private OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if ("google".equals(registrationId)) {
            return new GoogleOAuth2UserInfo(attributes);
        } else if ("naver".equals(registrationId)) {
            return new NaverOAuth2UserInfo(attributes);
        }
        throw new OAuth2AuthenticationException("지원하지 않는 로그인 방식입니다: " + registrationId);
    }

    private User registerNewUser(OAuth2UserInfo userInfo, String registrationId) {
        Provider provider = Provider.valueOf(registrationId.toUpperCase());
        User user = User.builder()
                .email(userInfo.getEmail())
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .provider(provider)
                .name(userInfo.getName())
                .role(resolveRole(userInfo.getEmail()))
                .build();

        return userRepository.save(user);
    }

    private Role resolveRole(String email) {
        return adminEmails.contains(email) ? Role.ADMIN : Role.CITIZEN;
    }
}
