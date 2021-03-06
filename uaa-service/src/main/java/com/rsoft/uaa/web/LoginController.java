package com.rsoft.uaa.web;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.rsoft.uaa.dto.User;

@RestController
public class LoginController {
	
	@PostMapping("/login")
	public String login(@RequestBody User u, HttpServletResponse response){
		User existed = users.get(u.getUsername());
		if(existed != null && existed.getPassword().equals(u.getPassword())){
			response.setHeader("_uid_", existed.getUserid());
			return existed.getUserid();
		}
		return null;
	}
	
	@GetMapping("/user")
	public User getUser(String username){
		return users.get(username);
	}
	
	private static Map<String,User> users = new ConcurrentHashMap<String,User>();
	
	@PostConstruct 
	private void init(){
		users.put("admin", new User("u001","admin","admin"));
		users.put("test", new User("u005","test","123456"));
		users.put("demo", new User("u093","demo","123456"));
	}
}
