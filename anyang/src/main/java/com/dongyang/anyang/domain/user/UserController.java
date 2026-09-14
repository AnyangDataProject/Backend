package com.dongyang.anyang.domain.user;

import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("api/admin/users")
    public ResponseEntity<List<UserResponseDto>> getAllUsers(){
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/api/admin/users/{id}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable Long id, @RequestBody UserStatusUpdateDto dto){
        userService.updateStatus(id, dto);

        return ResponseEntity.ok().build();
    }
}
