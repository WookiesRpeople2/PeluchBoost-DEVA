package com.example.spring_api.services;

import java.util.Random;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.example.spring_api.configuration.AsyncConfiguration;
import com.example.spring_api.repository.QuoteRepository;

import packages.Json.JsonMapper;
import packages.ORM.repository.BaseService;
import packages.ORM.repository.RepositoryServiceHandler;
import packages.ORM.repository.SetUpRepo;

@Service
public class QuoteService extends BaseService {
    @SetUpRepo
    private QuoteRepository quoetRepository;

    public CompletableFuture<String> getQuote(){
        return AsyncConfiguration.pool().submitAsync(() -> {
            try {
                return quoetRepository.findAll();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        })
                .thenApply(result -> JsonMapper.toJson(result.get(new Random().nextInt(result.size()))))
                .getFuture();
    }

}
