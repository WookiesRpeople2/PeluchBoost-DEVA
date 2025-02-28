package com.example.spring_api.configuration;

import com.example.spring_api.interfaces.Configimpl;

import io.github.cdimascio.dotenv.Dotenv;

public record DataBaseConfiguration(String dbUrl, String dbUser, String dbPassword, String modelDirectory) implements Configimpl {

    public DataBaseConfiguration(Dotenv dotenv) {
        this(dotenv.get("DATABASE_URL"), dotenv.get("DATABASE_USER"), dotenv.get("DATABASE_PASSWORD"), "com.example.spring_api.models");
    }

    @Override
    public void validate() {
        if (dbUrl == null)
            throw new RuntimeException("DBUrl is missing");
        if(dbUser == null)
            throw new RuntimeException("DBUser is missing");
        if(dbPassword == null)
            throw new RuntimeException("DBPassword is missing");
        if(!dbUrl.startsWith("jdbc:"))
            throw new RuntimeException("Something went wrong with the values in the .env file");
    }
}
