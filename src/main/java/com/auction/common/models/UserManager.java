package com.auction.common.models;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class UserManager {
    private static UserManager instance;
    private List<User> users;
    private final String DATA_FILE = "users.dat";
    private long nextId = 1;
    private User currentUser;

    private UserManager() {
        users = new ArrayList<>();
        loadData();
        if (users.isEmpty()) {
            register(new Admin(String.valueOf(generateId()), "admin", "123", "System Admin", "admin@mail"));
            register(new Seller(String.valueOf(generateId()), "seller1", "123", "Seller One", "seller@mail"));
            register(new Bidder(String.valueOf(generateId()), "bidder1", "123", "Bidder One", "bidder@mail", 1_000_000));
        }
    }

    public static UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    public long generateId() {
        return nextId++;
    }

    public User login(String username, String password) {
        for (User u : users) {
            if (u.getUsername().equals(username) && u.getPassword().equals(password)) {
                return u;
            }
        }
        return null;
    }

    public boolean register(User newUser) {
        for (User u : users) {
            if (u.getUsername().equals(newUser.getUsername())) {
                return false;
            }
        }
        users.add(newUser);
        saveData();
        return true;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(users);
            oos.writeLong(nextId);
        } catch (IOException e) {
            System.out.println("Loi luu file: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void loadData() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            users = (List<User>) ois.readObject();
            nextId = ois.readLong();
        } catch (Exception e) {
            users = new ArrayList<>();
        }
    }
}
