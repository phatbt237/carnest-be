package com.example.carnest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CarnestApplication {

	public static void main(String[] args) {
		SpringApplication.run(CarnestApplication.class, args);
	}

}
