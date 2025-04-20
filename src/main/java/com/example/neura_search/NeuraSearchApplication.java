package com.example.neura_search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class NeuraSearchApplication {

	public static void main(String[] args) {
		SpringApplication.run(NeuraSearchApplication.class, args);
	}

}
