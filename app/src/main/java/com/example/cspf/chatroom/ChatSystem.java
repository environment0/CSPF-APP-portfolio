package com.example.cspf.chatroom;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Date;

public class ChatSystem {

    // UserType Enum
    public enum UserType {
        user("user"),
        expert("expert");

        private final String value;

        UserType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    // ChatType Enum
    public enum ChatType {
        TEXT("text"),
        IMG("img"),
        FILE("file"),
        PET("pet");

        private final String value;

        ChatType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    // User Class
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class User {
        private String nickname;
        private String profileImg;
        private String userCode;
    }

    // Pet Class
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Pet {
        private String petName;
        private String ownerName;
        private String kindName;
        private Date birthday;
        private String gender;
        private boolean neuter;
    }

    // Chat Class
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Chat {
        private UserType userType;
        private ChatType msgType;
        private User profile;
        private String msg;
        private String imgUrl;
        private Pet petMsg;
        private Date date;
    }
}