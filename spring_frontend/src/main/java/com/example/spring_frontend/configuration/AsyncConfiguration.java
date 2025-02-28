package com.example.spring_frontend.configuration;

import packages.Thread.Async.AsyncPool;

public class AsyncConfiguration {
    private static AsyncPool pool;

    public static AsyncPool pool() {
        if (pool == null) {
            pool = new AsyncPool();
        }
        return pool;
    }
}
