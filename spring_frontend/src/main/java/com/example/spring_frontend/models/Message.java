package com.example.spring_frontend.models;

import packages.Json.Annotation.JsonField;
import packages.ORM.annotations.Column;
import packages.ORM.annotations.Entity;
import packages.ORM.annotations.RelationType;
import packages.ORM.annotations.Relationship;

import java.sql.Date;
import java.sql.Timestamp;

@Entity
public class Message {
    @Column(primaryKey = true, unique = true)
    @JsonField
    private String id;

    @Column
    @JsonField
    private final String content;

    @Column
    @JsonField
    private final String response;

    @Relationship(type = RelationType.MANY_TO_ONE, targetEntity = User.class)
    private User user;

    @Column(type = "TIMESTAMP", defaultValue = "CURRENT_TIMESTAMP")
    private Timestamp createdAt;

    private Message(String content, String response, User user) {
        this.content = content;
        this.response = response;
        this.user = user;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String content;
        private String response;
        private User user;

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder response(String response) {
            this.response = response;
            return this;
        }

        public Builder user(User user) {
            this.user = user;
            return this;
        }

        public Message build() {
            return new Message(content, response, user);
        }
    }

    public String getContent() {
        return content;
    }

    public String getResponse() {
        return response;
    }

    public User getUser() {
        return user;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }
}
