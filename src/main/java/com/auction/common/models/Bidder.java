package com.auction.common.models;
public class Bidder extends User {
    private double balance;
    public Bidder(String id, String username, String password, String fullName, String email, double balance) {
        super(id, username, password, fullName, email);
        this.balance = balance;
    }
    @Override
    public String getRole() {
        return "BIDDER";
    }
    @Override
    public void printInfo() {
        super.printInfo();
        System.out.println("Số dư: " + balance);
    }
}