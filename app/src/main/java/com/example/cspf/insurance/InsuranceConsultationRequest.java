package com.example.cspf.insurance;

import lombok.Data;

@Data
public class InsuranceConsultationRequest {
    private String insurerId;  // 보험사 코드
    private String pet;        // 강아지 등록번호

    public InsuranceConsultationRequest(String insurerId, String pet) {
        this.insurerId = insurerId;
        this.pet = pet;
    }
}