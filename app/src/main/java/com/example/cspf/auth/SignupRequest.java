package com.example.cspf.auth;

import lombok.Data;

@Data
public class SignupRequest {
    private String username;
    private String password;
    private String name;
    private String nickname;
    private String email;
    private String addr;
    private String phone;
    private String gender;
    private String profileImage;  // 프로필 이미지 경로 필드 추가

    // 기존 생성자
    public SignupRequest(String username, String password, String name, String nickname, String email, String addr, String phone, String gender) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.nickname = nickname;
        this.email = email;
        this.addr = addr;
        this.phone = phone;
        this.gender = gender;
    }

    // 프로필 이미지를 포함한 새로운 생성자
    public SignupRequest(String username, String password, String name, String nickname, String email, String addr, String phone, String gender, String profileImage) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.nickname = nickname;
        this.email = email;
        this.addr = addr;
        this.phone = phone;
        this.gender = gender;
        this.profileImage = profileImage;
    }
}