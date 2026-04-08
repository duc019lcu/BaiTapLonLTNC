package com.auction.common.models;
import java.io.Serializable;
public abstract class User implements Serializable {
    private String id;
    private String username;
    private String password;
    private String fullName;
    private String email;
    public User(String id, String username, String password, String fullName, String email) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
    }
    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public abstract String getRole();
    public void printInfo() {
        System.out.println("User: " + fullName + " [" + getRole() + "]");
    }