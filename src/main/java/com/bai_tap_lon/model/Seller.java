package com.auction.onlineauctionsystem.model;
public class Seller extends User {
    private double reputation; 
    private int totalItemsSold;
    public Seller(long id, String username, String password, String email) {
        super(id, username, password, email);
        this.reputation = 0.0; // Mặc định mới tạo là 0
        this.totalItemsSold = 0;
    }
    @Override
    public String getRoleName() { return "Seller"; }
}
