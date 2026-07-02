package com.example.cspf.auth;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private String message;
    private String username;
    private String nickname; // 추가
    private String accessToken;
    private String refreshToken;
}