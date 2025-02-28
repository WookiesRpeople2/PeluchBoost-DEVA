package com.example.spring_api.controllers;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.spring_api.services.QuoteService;

@RestController
@RequestMapping("/quote")
public class QuoteController {
    @Autowired
    private QuoteService quoteService;

    @GetMapping
    public CompletableFuture<String> getQuote(){
        return quoteService.getQuote();
    }
}
