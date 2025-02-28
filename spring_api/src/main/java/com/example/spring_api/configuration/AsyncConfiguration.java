package com.example.spring_api.configuration;


import org.springframework.context.annotation.Configuration;

import packages.Thread.Async.AsyncPool;

@Configuration
public class AsyncConfiguration {
    private static AsyncPool pool;

    public static AsyncPool pool() {
        if (pool == null) {
          pool = new AsyncPool();
        }
        return pool;
    }
}
