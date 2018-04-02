package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.IOException;

@SpringBootApplication
public class SpringSpark2Application implements CommandLineRunner {
    public static final String PATH =  System.getProperty("user.dir");
    public static final String PATH_ =  System.setProperty("app.home.path", PATH);
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static String Path2;


	public static void main(String[] args) throws IOException {

        File currentDirectory = new File(new File(".").getAbsolutePath());
        Path2 = currentDirectory.getCanonicalPath();

        System.setProperty("app.home.path", System.getProperty("user.dir"));
		SpringApplication.run(SpringSpark2Application.class, args);
	}

	@Override
	public void run(String... strings) throws Exception {
		logger.info("App is started...");
		logger.info("PATH = " + PATH);
		logger.info("Path2 = " + Path2);
		logger.info("Path3 = " + getClass().getProtectionDomain().getCodeSource().getLocation());

	}
}
