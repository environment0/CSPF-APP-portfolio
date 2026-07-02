package com.example.cspf.chatbot;

import lombok.Data;

@Data
public class ReservationResponse {
    private String reservationId;
    private String status;
    private String message;
}