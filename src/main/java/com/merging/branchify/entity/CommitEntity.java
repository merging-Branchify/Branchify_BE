package com.merging.branchify.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Getter;

@Getter
@Entity
public class CommitEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String sha;
    private String message;
    private String author;
    private String date;

    public void setId(Long id) {
        this.id = id;
    }
    public void setSha(String sha) {
        this.sha = sha;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public void setAuthor(String author) {
        this.author = author;
    }
    public void setDate(String date) {
        this.date = date;
    }
}
