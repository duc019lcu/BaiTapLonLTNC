package com.auction;

import com.auction.domain.AuctionSession;
import com.auction.domain.AuctionStatus;

// Demo tạm; sau có thể tách Main cho server / JavaFX Application.
public class Main {
    public static void main(String[] args) {
        AuctionSession session = new AuctionSession("001", "LAPTOP", "TRANDUC", 500.0);

        session.setStatus(AuctionStatus.RUNNING);
        System.out.println("--- Bắt đầu đấu giá ---");

        session.processBid("User_A", 520.0);
        session.processBid("User_B", 510.0);
        session.processBid("User_C", 600.0);
    }
}
