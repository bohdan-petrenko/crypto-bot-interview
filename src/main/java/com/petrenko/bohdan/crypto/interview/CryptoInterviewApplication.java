package com.petrenko.bohdan.crypto.interview;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class CryptoInterviewApplication {

	public static void main(String[] args) {
		SpringApplication.run(CryptoInterviewApplication.class, args);
	}

}
