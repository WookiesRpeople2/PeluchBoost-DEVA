package com.example.spring_frontend.models;

import java.util.ArrayList;
import java.util.List;

import packages.Json.Annotation.JsonField;
import packages.ORM.annotations.Column;
import packages.ORM.annotations.Entity;
import packages.ORM.annotations.RelationType;
import packages.ORM.annotations.Relationship;

@Entity
public class User{
  @Column(primaryKey = true)
  private String id;

  @Column
  @JsonField
  private String username;

//  @Column
//  @Relationship(type = RelationType.ONE_TO_MANY, targetEntity = Message.class)
//  private List<Message> messages;

  private User(String username) {
    this.username = username;
//    this.messages = messages;
  }

  public static Builder builder() {
    return new User.Builder();
  }

  public static class Builder {
    private String username;

    public Builder username(String username) {
      this.username = username;
      return this;
    }


    public User build() {
      return new User(username);
    }
  }

  public String getId() {
    return id;
  }

  public String setId(String id){
    return this.id = id;
  }

  public String getUsername() {
    return username;
  }
}
