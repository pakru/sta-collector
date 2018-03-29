package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringSpark2Application implements CommandLineRunner {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public static void main(String[] args) {
		SpringApplication.run(SpringSpark2Application.class, args);
	}

	@Override
	public void run(String... strings) throws Exception {
		logger.info("App is started...");

	}
}
