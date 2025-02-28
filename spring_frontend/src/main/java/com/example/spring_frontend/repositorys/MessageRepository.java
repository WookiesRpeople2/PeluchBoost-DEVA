package com.example.spring_frontend.repositorys;

import com.example.spring_frontend.models.Message;

import com.example.spring_frontend.models.User;
import packages.ORM.repository.BaseRepository;

import java.util.List;

public interface MessageRepository extends BaseRepository<Message, String>{
  Message save(Message data);
  List<Message> findAllByUserId(String userId);
}
