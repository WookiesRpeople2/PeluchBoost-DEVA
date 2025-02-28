package com.example.spring_api.models;

import packages.Json.Annotation.JsonField;
import packages.ORM.annotations.Column;
import packages.ORM.annotations.Entity;

@Entity
public class Quote{
  @JsonField
  @Column(primaryKey = true, unique = true)
  private String id;

  @JsonField
  @Column
  private String quote;

  @JsonField
  @Column
  String author;
}
