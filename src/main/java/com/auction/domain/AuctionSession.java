package com.auction.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AuctionSession {
    private String auctionID;
    private String itemID;
    private String sellerID;
    private double currentHighestBid;
    private String winnerID;
    private String startTime;
    private String endTime;
    private AuctionStatus status;
    private final List<BidTransaction> bidHistory;

    public AuctionSession(String auctionID, String itemID, String sellerID, double startPrice) {
        this.auctionID = auctionID;
        this.itemID = itemID;
        this.sellerID = sellerID;
        this.currentHighestBid = startPrice;
        this.winnerID = "None";
        this.status = AuctionStatus.OPEN;
        this.bidHistory = new ArrayList<>();
    }

    public String getAuctionID() {
        return auctionID;
    }

    public void setAuctionID(String auctionID) {
        this.auctionID = auctionID;
    }

    public String getItemID() {
        return itemID;
    }

    public void setItemID(String itemID) {
        this.itemID = itemID;
    }

    public String getSellerID() {
        return sellerID;
    }

    public void setSellerID(String sellerID) {
        this.sellerID = sellerID;
    }

    public double getCurrentHighestBid() {
        return currentHighestBid;
    }

    public void setCurrentHighestBid(double currentHighestBid) {
        this.currentHighestBid = currentHighestBid;
    }

    public String getWinnerID() {
        return winnerID;
    }

    public void setWinnerID(String winnerID) {
        this.winnerID = winnerID;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public AuctionStatus getStatus() {
        return status;
    }

    public void setStatus(AuctionStatus status) {
        this.status = status;
    }

    public synchronized boolean processBid(String bidderID, double bidAmount) {
        if (this.status != AuctionStatus.RUNNING && this.status != AuctionStatus.EXTENDED) {
            System.out.println("Phiên đấu giá đang không trong trạng thái nhận giá!");
            return false;
        }

        if (bidAmount <= this.currentHighestBid) {
            System.out.println("Giá đặt " + bidAmount + " phải cao hơn giá hiện tại!");
            return false;
        }

        this.currentHighestBid = bidAmount;
        this.winnerID = bidderID;
        this.bidHistory.add(new BidTransaction(this.auctionID, bidderID, bidAmount, LocalDateTime.now()));

        System.out.println("Cập nhật giá thành công!");
        return true;
    }

    public synchronized List<BidTransaction> getBidHistory() {
        return Collections.unmodifiableList(new ArrayList<>(bidHistory));
    }
}
