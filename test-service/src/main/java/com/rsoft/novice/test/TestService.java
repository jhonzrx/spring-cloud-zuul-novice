package com.rsoft.novice.test;

import java.util.Random;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EnableDiscoveryClient
@SpringBootApplication
public class TestService {

	public static void main(String[] args) {
		SpringApplication.run(TestService.class, args);
	}
	
	@RequestMapping("test")
	public String test(String name){
		return (name==null?"":name)+(new Random()).nextInt(999999);
	}
}
