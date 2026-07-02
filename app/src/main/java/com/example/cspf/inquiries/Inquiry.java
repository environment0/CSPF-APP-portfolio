package com.example.cspf.inquiries;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inquiry {
    private int id;
    private String title;
    private String content;
    private String createdAt;
    private String authorCode;
}
