package com.example.spring_frontend.repositorys;

import com.example.spring_frontend.models.User;
import packages.ORM.repository.BaseRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends BaseRepository<User, String> {
    Optional<User> findByUsername(String name);
    User save(User user);
    List<User> findAll();
}
