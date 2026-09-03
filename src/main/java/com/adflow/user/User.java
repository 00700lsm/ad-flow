package com.adflow.user;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ad_users")
public class User {

    @Id
    private Long id;
    private int age;
    private String preferredCategories;

    protected User() {
    }

    public User(Long id, int age, String preferredCategories) {
        this.id = id;
        this.age = age;
        this.preferredCategories = preferredCategories;
    }

    public Long getId() {
        return id;
    }

    public int getAge() {
        return age;
    }

    public String getPreferredCategories() {
        return preferredCategories;
    }
}
