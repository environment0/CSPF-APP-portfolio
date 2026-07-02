package com.example.cspf.chatroom;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccessUser {
    private String owner;
    private String[] access;
    private String[] invite;
}
