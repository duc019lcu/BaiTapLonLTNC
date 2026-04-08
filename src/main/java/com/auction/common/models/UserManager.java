package com.auction.onlineauctionsystem.model;
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
            register(new Admin(generateId(), "admin", "123", "admin@mail", 1));
            register(new Seller(generateId(), "seller1", "123", "seller@mail"));
            register(new Bidder(generateId(), "bidder1", "123", "bidder@mail", "Hà Nội"));
            System.out.println("[Hệ thống] Đã tạo dữ liệu mẫu thành công.");
        }
    }
    public static UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }
    // Tạo ID tự động
    public long generateId() {
        return nextId++;
    }
    // Đăng nhập
    public User login(String username, String password) {
        for (User u : users) {
            if (u.getUsername().equals(username) && u.getPassword().equals(password)) {
                return u;
            }
        }
        return null;
    }
    // Đăng ký
    public boolean register(User newUser) {
        // Kiểm tra trùng lặp username
        for (User u : users) {
            if (u.getUsername().equals(newUser.getUsername())) {
                return false; // Trùng tên
            }
        }
        users.add(newUser);
        saveData(); // Đăng ký xong tự động lưu ngay
        return true;
    }
    // Ghi file (Serialization)
    public void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(users);
            oos.writeLong(nextId); // Lưu luôn cái ID tiếp theo
        } catch (IOException e) {
            System.out.println("Lỗi lưu file: " + e.getMessage());
        }
    }
    // Đọc file
    @SuppressWarnings("unchecked")
    private void loadData() {
        File file = new File(DATA_FILE);
        if (!file.exists()) return;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            users = (List<User>) ois.readObject();
            nextId = ois.readLong(); // Đọc lại ID tiếp theo
            System.out.println("[Hệ thống] Đã tải " + users.size() + " người dùng từ file.");
        } catch (Exception e) {
            System.out.println("Lỗi đọc file, bắt đầu với danh sách trống.");
        }
    }
}