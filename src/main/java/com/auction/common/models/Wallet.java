package com.auction.onlineauctionsystem.model;
import java.io.Serializable;
public class Wallet implements Serializable {
    private static final long serialVersionUID = 1L;
    private double balance;       // Số dư khả dụng
    private double frozenAmount;  // Số tiền bị đóng băng khi đang đặt giá (theo des.docx)
    public Wallet() {
        this.balance = 0.0;
        this.frozenAmount = 0.0;
    }
    // Nạp tiền
    public void deposit(double amount) {
        if (amount > 0) balance += amount;
    }
    // Rút tiền (chỉ rút được phần không bị đóng băng)
    public boolean withdraw(double amount) {
        if (amount > 0 && balance >= amount) {
            balance -= amount;
            return true;
        }
        return false;
    }
    // Đóng băng tiền khi đặt giá (freeze)
    public boolean freeze(double amount) {
        if (amount > 0 && balance >= amount) {
            balance -= amount;
            frozenAmount += amount;
            return true;
        }
        return false;
    }
    // Mở khóa tiền (nếu bid thất bại) (release)
    public void release(double amount) {
        if (amount > 0 && frozenAmount >= amount) {
            frozenAmount -= amount;
            balance += amount;
        }
    }
    public double getBalance() { return balance; }
    public double getFrozenAmount() { return frozenAmount; }
}