package com.rsoft.gw;

import java.util.Random;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
//@EnableFeignClients
@SpringBootApplication
@EnableDiscoveryClient
public class GatewyNovice {
	public static void main(String[] args){
		SpringApplication.run(GatewyNovice.class, args);
	}
	
	@GetMapping("/health")
	public String heart(){
		return ""+(new Random()).nextInt(99999);
	}
	
	@GetMapping("/fallback")
	public String fallback() {
		return "error";
	}
}
