package com.example.spring_frontend.services;

import com.example.spring_frontend.configuration.AsyncConfiguration;
import com.example.spring_frontend.models.User;
import com.example.spring_frontend.repositorys.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import packages.ORM.repository.BaseService;
import packages.ORM.repository.RepositoryServiceHandler;
import packages.ORM.repository.SetUpRepo;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class UserService extends BaseService {
    @Autowired
    @SetUpRepo
    private UserRepository userRepository;

    public CompletableFuture<User> findOrCreateUser(User user){
        return AsyncConfiguration.pool().submitAsync(() -> {
            try {
                Optional<User> foundUser = userRepository.findByUsername(user.getUsername());
                return foundUser.orElseGet(() -> userRepository.save(user));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).getFuture();
    }

    public CompletableFuture<List<User>> getAllUsers() {
        return AsyncConfiguration.pool().submitAsync(() -> {
            try {
                return userRepository.findAll();
            } catch (Exception e) {
                throw new RuntimeException("Failed to fetch users: " + e.getMessage(), e);
            }
        }).getFuture();
    }
}
