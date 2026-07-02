package com.example.cspf.chatbot;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LawyerChatRequest {
    private String lawyerCode;
    private String reqDate;
    private String prefTime;
    private String description;
}
