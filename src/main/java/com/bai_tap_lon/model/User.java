package com.auction.onlineauctionsystem.model;

import java.io.Serializable;

/**
 * Lớp User trừu tượng - Là cha của Admin, Bidder và Seller.
 * Kế thừa từ Entity để quản lý ID tập trung.
 */
public abstract class User extends Entity implements Serializable {
    private static final long serialVersionUID = 1L; // Đảm bảo đồng bộ khi lưu file
    protected String username;
    protected String password;
    protected String email;
    protected Wallet wallet; // Mỗi người dùng có một ví tiền riêng
    // Constructor đầy đủ
    public User(long id, String username, String password, String email) {
        super(id); // Gọi constructor của lớp cha Entity để gán ID
        this.username = username;
        this.password = password;
        this.email = email;
        this.wallet = new Wallet(); // Khởi tạo ví mới ngay khi tạo người dùng
    }
    // Getter và Setter cho các thuộc tính
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public Wallet getWallet() {
        return wallet;
    }
    // Phương thức trừu tượng để các lớp con (Admin, Bidder, Seller) định nghĩa vai trò
    public abstract String getRoleName();
    // Bạn có thể thêm các phương thức chung ở đây, ví dụ: hiển thị thông tin cơ bản
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", role='" + getRoleName() + '\'' +
                '}';
    }
}