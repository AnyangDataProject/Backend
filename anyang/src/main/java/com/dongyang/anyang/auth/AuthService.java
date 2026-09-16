package com.dongyang.anyang.auth;

import com.dongyang.anyang.domain.user.Provider;
import com.dongyang.anyang.domain.user.Role;
import com.dongyang.anyang.domain.user.User;
import com.dongyang.anyang.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Value("#{'${admin.emails}'.split(',\\s*')}")
    private List<String> adminEmails;

    public void signup(AuthDto.SignupRequest request) {
        if(userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .provider(Provider.LOCAL)
                .name(request.name())
                .phone(request.phone())
                .role(resolveRole(request.email()))
                .build();

        userRepository.save(user);
    }

    public AuthDto.TokenResponse login(AuthDto.LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));

        if(user.getStatus() == User.UserStatus.SUSPENDED) {
            throw new IllegalStateException("이용이 제한된 계정입니다. 관리자에게 문의해주세요.");
        }

        if(user.getPassword() == null) {
            throw new IllegalArgumentException("이 계정은 소셜 로그인으로 가입되었습니다. Google/Naver로 로그인해주세요.");
        }

        if(!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        Role syncedRole = resolveRole(user.getEmail());
        if(syncedRole != user.getRole()) {
            user.updateRole(syncedRole);
        }

        String token = jwtProvider.createToken(user.getId(), user.getEmail(), user.getRole());
        user.updateLastLogin();
        return new AuthDto.TokenResponse(token, user.getEmail(), user.getName(), user.getRole().name());
    }

    private Role resolveRole(String email) {
        return adminEmails.contains(email) ? Role.ADMIN : Role.CITIZEN;
    }
}
