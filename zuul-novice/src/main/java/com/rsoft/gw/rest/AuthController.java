package com.rsoft.gw.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.rsoft.gw.support.auth.AuthService;

@RestController
public class AuthController {
	@Autowired
	private AuthService authService;
	
}
