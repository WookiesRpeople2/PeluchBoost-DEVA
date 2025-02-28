package com.example.spring_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import packages.ORM.config.DatabaseConfig;

@SpringBootApplication
public class SpringApiApplication {
	private static final String URL = Config.getInstance().getDb().dbUrl();
	private static final String USER = Config.getInstance().getDb().dbUser();
	private static final String PASSWORD = Config.getInstance().getDb().dbPassword();
	private static final String MODELPATH = Config.getInstance().getDb().modelDirectory();

	public static void main(String[] args) {
		DatabaseConfig.initialize(new DatabaseConfig.Builder()
				.url(URL)
				.username(USER)
				.password(PASSWORD)
				.basePackage(MODELPATH)
				.build());
		SpringApplication.run(SpringApiApplication.class, args);
	}

}
