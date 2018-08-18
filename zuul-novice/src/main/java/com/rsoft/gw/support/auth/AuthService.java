package com.rsoft.gw.support.auth;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("uaa")
public interface AuthService {
	@RequestMapping(value = "/login", method = RequestMethod.POST)
	String login(@RequestParam("username") String username, @RequestParam("password") String password);
}
