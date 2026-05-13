package com.auction.domain;

import java.time.LocalDateTime; //ngày giờ hiện tại
import java.time.Duration; // tính khoảng cách thời gian giữa hai thời điểm
import java.time.format.DateTimeFormatter; // định dạng kiểu chuỗi thành ngày - giờ và ngược lại
import java.util.ArrayList; // mảng động, dùng để lưu trữc các tập hợp dữ liệu
import java.util.Collections;  // sort, reverse
import java.util.List; // interface chung

public class AuctionSession {
    private String auctionID; //ID phiên đấu giá
    private String itemID; // ID đồ bán
    private String itemName; // Tên đồ bán
    private String sellerID; // ID người bán
    private double currentHighestBid; // giá đang đặt cao nhất
    private String currentHighestBidderID; // ID người đặt giá cao nhất
    private String winnerID; // ID người thắng
    private LocalDateTime startTime; // giờ bắt đầu
    private LocalDateTime endTime; // giờ kết thúc
    private AuctionStatus status; // trạng thái
    private List<BidTransaction> bidHistory; // lịch sử tất cả các lần đặt giá
    private static final long ANTI_SNIPING_SECONDS = 30; // Số giây cuối phiên để kích hoạt gia hạn
    private static final long EXTENSION_SECONDS = 60; // Thêm bao nhiêu giây khi gia hạn

    public AuctionSession(String auctionID, String itemID, String sellerID, double startPrice,
                         LocalDateTime startTime, LocalDateTime endTime) {
        this(auctionID, itemID, itemID, sellerID, startPrice, startTime, endTime);
    }

    public AuctionSession(String auctionID, String itemID, String itemName, String sellerID, double startPrice,
                          LocalDateTime startTime, LocalDateTime endTime) {
        this.auctionID = auctionID;
        this.itemID = itemID;
        this.itemName = itemName;
        this.sellerID = sellerID;
        this.currentHighestBid = startPrice;
        this.currentHighestBidderID = null;
        this.winnerID = null;
        this.startTime = startTime;
        this.endTime = endTime;
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

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getDisplayItem() {
        return itemName != null && !itemName.isBlank() ? itemName : itemID;
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

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public AuctionStatus getStatus() {
        return status;
    }

    public void setStatus(AuctionStatus status) {
        this.status = status;
    }

    // synchronized giúp đảm bảo tại một thời điểm chỉ có duy nhất 1 người được thực hiện đặt giá, 
    // tránh việc hai người cùng đặt một lúc dẫn đến loạn số liệu
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

        checkAndExtendTime();
        
        System.out.println("Cập nhật giá thành công!");
        return true;
    }
    // nếu trạng thái khác running hoặc extended thì in ra thông báo
    // còn không thực hiện tiếp khối bên dưới, nếu giá đặt nhỏ hơn giá cao nhất đã đặt thì in ra thông báo
    // không có gì thì xuống hai dòng dưới, gán giá cao nhất cho giá vừa đặt và winnerID thành bidderID
    // gọi hàm mở rộng thời gian
    // in ra thông báo nếu đặt giá thành công
    
    private void checkAndExtendTime(){
        if (this.endTime == null || this.status == AuctionStatus.FINISHED) return;

        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime end = this.endTime;
            Duration duration = Duration.between(now, end);
            long secondsLeft = duration.getSeconds();

            if (secondsLeft > 0 && secondsLeft <= 30) {
                LocalDateTime newEndTime = end.plusSeconds(60);
                this.endTime = newEndTime;
                this.status = AuctionStatus.EXTENDED;
                System.out.println("[HỆ THỐNG]: Tự động gia hạn thêm 60s");
            }
    }   catch (Exception e){
            System.out.println("Lỗi xử lý thời gian: " + e.getMessage());
    }
    // Nếu chưa cài đặt thời gian kết thúc hoặc phiên đã xong thì không cần đến hàm này
    // Lấy thời gian hiện tại và biến chuỗi endtime về định dạng ngày giờ
    // Tính toán khoảng cách thời gian từ lúc bắt đầu đến kết thúc phiên bằng duration(giúp tính chuẩn hơn, đỡ phải đổi đơn vị)
    // Nếu thời gian lớn hơn 0 nhỏ hơn hoặc bằng 30 mà vẫn có người đặt, thì cộng thêm 60 giây, in ra thông báo
    // nếu việc parse thời gian bị lỗi định dạng, in ra thông báo lỗi
}
}

