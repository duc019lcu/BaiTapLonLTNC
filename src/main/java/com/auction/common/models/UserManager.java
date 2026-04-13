package com.auction.common.models;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
public class UserManager {
    private static UserManager instance;
    private List<User> users;
    private final String DATA_FILE = "users.dat";
    private long nextId = 1; // Biến tự động tăng ID cho user mới
    // Singleton Pattern
    private UserManager() {
        users = new ArrayList<>();
        loadData();
        // DỮ LIỆU MẪU (Seed Data): Tự tạo tài khoản nếu file trống
        if (users.isEmpty()) {
            register(new Admin(String.valueOf(generateId()), "admin", "123", "System Admin", "admin@mail"));
            register(new Seller(String.valueOf(generateId()), "seller1", "123", "Seller One", "seller@mail"));
            register(new Bidder(String.valueOf(generateId()), "bidder1", "123", "Bidder One", "bidder@mail", 1_000_000));
            System.out.println("[Hệ thống] Đã tạo dữ liệu mẫu thành công.");
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
                return false; // Trùng tên
            }
        }
        users.add(newUser);
        saveData();
        return true;
    }
    public void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(users);
            oos.writeLong(nextId);
        } catch (IOException e) {
            System.out.println("Lỗi lưu file: " + e.getMessage());
        }
    }
    @SuppressWarnings("unchecked")
    private void loadData() {
        File file = new File(DATA_FILE);
        if (!file.exists()) return;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            users = (List<User>) ois.readObject();
            nextId = ois.readLong();
            System.out.println("[Hệ thống] Đã tải " + users.size() + " người dùng từ file.");
        } catch (Exception e) {
            System.out.println("Lỗi đọc file, bắt đầu với danh sách trống.");
        }
    }
}