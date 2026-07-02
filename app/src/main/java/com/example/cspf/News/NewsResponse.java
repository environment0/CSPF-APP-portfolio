package com.example.cspf.News;


import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
public class NewsResponse {
    @SerializedName("news_author")  // 서버 응답에 맞는 필드 이름
    private String newsAuthor;

    @SerializedName("news_contents")  // 서버 응답에 맞는 필드 이름
    private String newsContents;



    @Override
    public String toString() {
        return "Author: " + newsAuthor + ", Content: " + newsContents;
    }
}
