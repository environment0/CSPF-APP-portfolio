package com.example.cspf.notices;

import java.util.List;

public class NoticeResponse {

    private List<Notice> data;  // 서버 응답의 "data" 필드를 가리킵니다.

    // Getter와 Setter
    public List<Notice> getData() {
        return data;
    }

    public void setData(List<Notice> data) {
        this.data = data;
    }
}
