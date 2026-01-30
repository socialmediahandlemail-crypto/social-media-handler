package com.example.SocialSync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SocialSyncApplication {

	public static void main(String[] args) {
		SpringApplication.run(SocialSyncApplication.class, args);
	}

}
