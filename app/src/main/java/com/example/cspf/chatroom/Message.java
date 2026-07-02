package com.example.cspf.chatroom;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Message {
    private String sender;      // 송신자 이름
    private String message;     // 텍스트 메시지
    private String imageUrl;    // 이미지 URL (이미지가 없으면 null로 설정)
    public boolean isImageMessage() {
        return imageUrl != null && !imageUrl.isEmpty();
    }

    public boolean isTextMessage() {
        return message != null && !message.isEmpty();
    }
}
