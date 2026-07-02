package com.example.cspf.chatbot;

import lombok.Data;

@Data
public class VetReservationRequest {
    private String resvDate;
    private String prefTime;
    private String description;
    private String pet;
    private String hospId;  // 추가

    public VetReservationRequest(String resvDate, String prefTime, String description, String pet, String hospId) {
        this.resvDate = resvDate;
        this.prefTime = prefTime;
        this.description = description;
        this.pet = pet;
        this.hospId = hospId;
    }
}