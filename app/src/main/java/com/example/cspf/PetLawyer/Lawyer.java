package com.example.cspf.PetLawyer;

import lombok.Data;

@Data
public class Lawyer {
    private String expertCode; // 변호사 코드
    private String name;
    private String phone;
    private String email;
    private String company;
    private String workexperience; // 경력
    private String companyaddr; // 회사 주소
    private String product; // 주요 상품

    @Override
    public String toString() {
        return name + "\n연락처: " + phone + "\n이메일: " + email + "\n회사: " + company;
    }
}
