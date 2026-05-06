package com.auction.common.models;

public class UserManager {
    private static UserManager instance;
    private User currentUser;

    private UserManager() {
        // Đã xóa bỏ logic đọc ghi file users.dat cũ
    }

    public static synchronized UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }
    
    public void logout() {
        this.currentUser = null;
    }
}
