package com.dongyang.anyang.auth;

public class AuthDto {

    public record SignupRequest(
            String email,
            String password,
            String name,
            String phone
    ) {}

    public record LoginRequest(
            String email,
            String password
    ) {}

    public record TokenResponse(
            String accessToken,
            String email,
            String name,
            String role
    ) {}
}
