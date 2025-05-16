package com.example.myservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MyserviceApplication {
	public static void main(String[] args) {
		SpringApplication.run(MyserviceApplication.class, args);
	}
}
