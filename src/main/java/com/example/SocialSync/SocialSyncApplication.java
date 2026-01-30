package com.example.SocialSync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SocialSyncApplication {

	public static void main(String[] args) {
		 System.out.println("DEBUG: MongoDB URI is " + System.getenv("MONGODB_URI"));
		SpringApplication.run(SocialSyncApplication.class, args);
	}

}
