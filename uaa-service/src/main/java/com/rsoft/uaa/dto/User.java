package com.rsoft.uaa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String userid;
    private String username;
    private String password;

//    public User(String userid, String username, String password) {
//        super();
//        this.userid = userid;
//        this.username = username;
//        this.password = password;
//    }
}