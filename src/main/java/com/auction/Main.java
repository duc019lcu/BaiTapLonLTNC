package com.auction;

import com.auction.domain.AuctionSession;
import com.auction.domain.AuctionStatus;

public class Main {
    public static void main(String[] args) {
        AuctionSession session = new AuctionSession("001", "LAPTOP", "SELLER1", 500.0);
        session.setStatus(AuctionStatus.RUNNING);

        System.out.println("--- Bat dau dau gia ---");
        session.processBid("User_A", 520.0);
        session.processBid("User_B", 510.0);
        session.processBid("User_C", 600.0);
    }
}
