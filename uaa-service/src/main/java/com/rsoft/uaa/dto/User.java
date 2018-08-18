package com.rsoft.uaa.dto;

import com.rsoft.uaa.web.LoginController;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class User{
	private String userid;
	private String username;
	private String password;
	public User(String userid, String username, String password) {
		super();
		this.userid = userid;
		this.username = username;
		this.password = password;
	}
}