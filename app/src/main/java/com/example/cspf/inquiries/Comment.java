package com.example.cspf.inquiries;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    private int id;
    private int boardId;
    private String authorCode;
    private String content;
    private String createdAt; // 댓글의 작성 날짜
    public Comment(String content) {
        this.content = content;
    }
}


