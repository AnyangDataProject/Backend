package com.dongyang.anyang.domain.user;

import com.dongyang.anyang.domain.report.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;

    public List<UserResponseDto> getAllUsers(){
        List<User> users = userRepository.findAll();

        return users.stream().map(
                user ->
                    UserResponseDto.builder()
                            .id(user.getId())
                            .email(user.getEmail())
                            .name(user.getName())
                            .phone(user.getPhone())
                            .role(user.getRole().name())
                            .status(user.getStatus().name())
                            .createdAt(user.getCreatedAt())
                            .lastLogin(user.getLastLogin())
                            .reportCount(reportRepository.countByUserId(user.getId()))
                            .build()
                            )
                            .toList();
    }

    public void updateStatus(Long userId, UserStatusUpdateDto dto){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없음"));

        user.updateStatus(dto.getStatus());
        userRepository.save(user);
    }
}
