package com.example.cspf.chatroom;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReqChatRoom {
    private List<ChatRoom> content;
}
