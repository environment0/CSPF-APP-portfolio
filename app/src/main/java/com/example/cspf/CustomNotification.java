package com.example.cspf;


import lombok.Data;

@Data
public class CustomNotification {
    private int id;
    private String title;
    private String content;
    private String createdAt;
    private String announcementAdminCode;

    // 기본 생성자
    public CustomNotification() {}

    // 매개변수를 받는 생성자 추가
    public CustomNotification(int id, String title, String content, String createdAt, String announcementAdminCode) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.announcementAdminCode = announcementAdminCode;
    }


}
