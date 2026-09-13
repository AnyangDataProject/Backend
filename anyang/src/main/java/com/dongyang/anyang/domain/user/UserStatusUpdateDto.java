package com.dongyang.anyang.domain.user;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserStatusUpdateDto {
    private User.UserStatus status;
}
