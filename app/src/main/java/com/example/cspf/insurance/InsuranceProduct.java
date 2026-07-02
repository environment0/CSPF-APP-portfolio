package com.example.cspf.insurance;

import lombok.Data;
import androidx.annotation.DrawableRes;

@Data
public class InsuranceProduct {
    private String insurerId;    // 보험사 코드
    private String name;
    private String description;
    private String price;
    private String coverage;
    @DrawableRes
    private int imageResId;

    public InsuranceProduct(String insurerId, String name, String description, String price, String coverage, int imageResId) {
        this.insurerId = insurerId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.coverage = coverage;
        this.imageResId = imageResId;
    }
}