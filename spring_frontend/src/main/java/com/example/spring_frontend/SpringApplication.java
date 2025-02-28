package com.example.spring_frontend;

import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringApplication{
  
  public static void main(String[] args){
    Application.launch(MessageApplication.class, args);
  }
}
