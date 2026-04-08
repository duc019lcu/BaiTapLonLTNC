package com.auction.domain;

public class AuctionSession {
    private String auctionID;
    private String itemID;
    private String sellerID;
    private double currentHighestBid;
    private String winnerID;
    private String startTime;
    private String endTime;
    private AuctionStatus status;

    public AuctionSession(String auctionID, String itemID, String sellerID, double startPrice) {
        this.auctionID = auctionID;
        this.itemID = itemID;
        this.sellerID = sellerID;
        this.currentHighestBid = startPrice;
        this.winnerID = "None";
        this.status = AuctionStatus.OPEN;
    }

    public double getCurrentHighestBid() {
        return currentHighestBid;
    }

    public String getWinnerID() {
        return winnerID;
    }

    public AuctionStatus getStatus() {
        return status;
    }

    public void setStatus(AuctionStatus status) {
        this.status = status;
    }

    public synchronized boolean processBid(String bidderID, double bidAmount) {
        if (status != AuctionStatus.RUNNING && status != AuctionStatus.EXTENDED) {
            System.out.println("Phien dau gia khong trong trang thai nhan gia");
            return false;
        }
        if (bidAmount <= currentHighestBid) {
            System.out.println("Gia dat phai cao hon gia hien tai");
            return false;
        }

        this.currentHighestBid = bidAmount;
        this.winnerID = bidderID;
        System.out.println("Cap nhat gia thanh cong");
        return true;
    }
}
