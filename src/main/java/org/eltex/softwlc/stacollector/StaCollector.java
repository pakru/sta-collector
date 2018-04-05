package org.eltex.softwlc.stacollector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class StaCollector implements CommandLineRunner {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public static void main(String[] args) throws IOException {
	    System.setProperty("app.config.path", "/etc/eltex-sta-collector");
		SpringApplication.run(StaCollector.class, args);
	}

	@Override
	public void run(String... strings) throws Exception {
		logger.info("App is started...");

	}
}
