package com.example.spring_frontend.configuration;

import org.springframework.web.reactive.function.client.WebClient;

public class Client{
  public static WebClient request(){
    return WebClient.builder()
      .baseUrl("http://localhost:8080")
      .defaultHeader("Content-Type", "application/json")
      .build();
  }
}
