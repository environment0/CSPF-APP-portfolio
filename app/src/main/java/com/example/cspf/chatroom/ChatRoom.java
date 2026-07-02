package com.example.cspf.chatroom;

import org.json.JSONObject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoom {
    private String chatRoomID; // 채팅방 고유번호
    private String chatRoom; // 채팅방명
    private JSONObject chatMemberCode; // 몰라
    private String creationTime; // 생성시각
    private String consultEndTime; // 채팅방
    private AccessUser accessUser; // 채팅방 주인 및 접속자 목록
}
