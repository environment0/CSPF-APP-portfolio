package com.example.cspf.auth;

import lombok.Data;

@Data
public class ExpertRequest {
    private String username;
    private String password;
    private String name;
    private String company;
    private String email;
    private String phone;
    private String expertType;
    private String certImage;
    private String image;  // 선택 사항: 프로필 사진
    private int credentialStatus = 0;  // 기본값으로 0 설정

    // 생성자 추가
    public ExpertRequest(String username, String password, String name, String company, String email, String phone, String expertType, String certImage) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.company = company;
        this.email = email;
        this.phone = phone;
        this.expertType = expertType;
        this.certImage = certImage;
    }

}
