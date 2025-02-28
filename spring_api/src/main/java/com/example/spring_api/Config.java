package com.example.spring_api;

import com.example.spring_api.configuration.DataBaseConfiguration;
import com.example.spring_api.interfaces.Configimpl;
import io.github.cdimascio.dotenv.Dotenv;

public class Config implements Configimpl {
    private static final Config config = new Config();
    private final DataBaseConfiguration db;


    private Config(){
        Dotenv dotenv = Dotenv.configure()
                .filename(".env")
                .load();
        this.db = new DataBaseConfiguration(dotenv);
        validate();
    }

    @Override
    public void validate() {
        db.validate();
    }

    public static Config getInstance(){
        return config;
    }

    public DataBaseConfiguration getDb(){
        return db;
    }
}
