package com.rsoft.uaa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class UaaService {
	public static void main(String[] args){
		SpringApplication.run(UaaService.class, args);
	}
}
