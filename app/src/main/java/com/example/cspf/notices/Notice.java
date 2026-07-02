package com.example.cspf.notices;

import lombok.Data;

@Data
public class Notice {

    private int id;  // 공지사항 ID
    private String content;  // 공지사항 내용
    private String createdAt;  // 공지사항 생성 날짜
    private String announcement_adminCode;  // 관리 코드
    private String title;  // 공지사항 제목


}
