package com.example.cspf.inquiries;

import org.json.JSONArray;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InquiryResponse {
    private List<Inquiry> data;
    private int total;
    private int page;
    private int limit;
}