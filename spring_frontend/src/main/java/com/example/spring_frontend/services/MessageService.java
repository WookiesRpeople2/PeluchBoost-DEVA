package com.example.spring_frontend.services;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.example.spring_frontend.configuration.AsyncConfiguration;
import com.example.spring_frontend.configuration.Client;
import com.example.spring_frontend.models.Message;
import com.example.spring_frontend.models.User;
import com.example.spring_frontend.repositorys.MessageRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import packages.Json.Parser.JsonObject;
import packages.Json.Parser.JsonParser;
import packages.ORM.repository.BaseService;
import packages.ORM.repository.RepositoryServiceHandler;
import packages.ORM.repository.SetUpRepo;


public class MessageService extends BaseService {
  @Autowired
  @SetUpRepo
  private MessageRepository messageRepository;

  public void saveMessage(Message message){
      AsyncConfiguration.pool().submitAsync(() -> {
          try {
              return messageRepository.save(message);
          } catch (Exception e) {
              throw new RuntimeException(e);
          }
      });
  }

  public CompletableFuture<String> getQuote(){
    return AsyncConfiguration.pool().submitAsync(()->{
      try {
        String json = Client.request()
          .get()
          .uri("/quote")
          .retrieve()
          .bodyToMono(String.class)
          .block();
        JsonObject jsonObject = new JsonParser(json).parse().asObject();
        return jsonObject.get("quote").asString();
      } catch (Exception e) {
        throw new RuntimeException("Failed to fetch quote: " + e.getMessage(), e);
      }
    }).getFuture();
  }

    public CompletableFuture<List<Message>> getMessagesByUser(String userId) {
        return AsyncConfiguration.pool().submitAsync(() -> {
            try {
                return messageRepository.findAllByUserId(userId);
            } catch (Exception e) {
                throw new RuntimeException("Failed to fetch messages: " + e.getMessage(), e);
            }
        }).getFuture();
    }
}

