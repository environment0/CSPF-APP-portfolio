package com.example.cspf;

import lombok.Data;

@Data
public class ExpertSignupRequest {
    private String username;
    private String password;
    private String name;
    private String phone;
    private String email;
    private String addr;
    private String gender;
    private String expertType;

    public ExpertSignupRequest(String username, String password, String name, String phone, String email, String addr, String gender, String expertType) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.addr = addr;
        this.gender = gender;
        this.expertType = expertType;
    }


}
