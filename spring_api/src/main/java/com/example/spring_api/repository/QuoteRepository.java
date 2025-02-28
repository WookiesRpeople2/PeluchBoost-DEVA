package com.example.spring_api.repository;

import com.example.spring_api.models.Quote;
import org.springframework.stereotype.Repository;
import packages.ORM.repository.BaseRepository;

import java.util.List;

@Repository
public interface QuoteRepository extends BaseRepository<Quote, String> {
    List<Quote> findAll();
}
