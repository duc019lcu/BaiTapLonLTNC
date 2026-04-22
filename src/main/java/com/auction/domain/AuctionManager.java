package com.auction.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors; // tạo threads chạy ngắn
import java.util.concurrent.ScheduledExecutorService; // lập lịch
import java.util.concurrent.TimeUnit; // đơn vị thời gian

public class AuctionManager {
    // volatile: đảm bảo giá trị của biến luôn được cập nhật chính xác giữa các luồng khác nhau.
    private static volatile AuctionManager instance;
    private final Map<String, AuctionSession> sessions;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1); 

    private AuctionManager() {
        this.sessions = new ConcurrentHashMap<>(); // Khởi tạo bộ lưu trữ an toàn đa luồng
        startAutoCloseTask(); // Kích hoạt ngay bộ quét thời gian tự động
    }

    public static AuctionManager getInstance() {
        if (instance == null) { //nếu đối tượng được tạo rồi, lấy dùng luôn, thay vì đợi các bước sau phức tạp
            synchronized (AuctionManager.class) { // sychronized đẻ giữ cho chỉ được phép 1 luồng truy cập
                if (instance == null) { // kiểm tra lần 2, không có thì phá vỡ nguyên tắc duy nhất của singleton
                    instance = new AuctionManager();
                }
            }
        }
        return instance;
    }

    // ScheduledExecutorService: cứ cách 1 giây thực hiện khối code 1 lần
    // ConcurrentHashMap: Bộ nhớ lưu trữ phiên đấu giá, an toàn khi nhiều người truy cập cùng lúc
    // Hàm trên để kiểm tra và đóng các phiên đấu giá đã hết giờ, không có cái này thì cá phiên đấu giá sẽ mãi hoạt động trừ khi kết thúc thủ công

    private void startAutoCloseTask() {
        scheduler.scheduleAtFixedRate(() -> { // cách 1 giây thực hiện khối code 1 lần
            LocalDateTime now = LocalDateTime.now();
            for (AuctionSession session : sessions.values()) {  // kiểm tra tất cả các phiên đấu giá đang lưu trong hệ thống
                if ((session.getStatus() == AuctionStatus.RUNNING || session.getStatus() == AuctionStatus.EXTENDED)
                        && session.getEndTime() != null) {

        // quét các phiên đang còn hoạt động để kết thúc phiên
                    try {
                        LocalDateTime endTime = LocalDateTime.parse(session.getEndTime());
                        if (now.isAfter(endTime)) {  // kiểm tra xem kết thúc chưa thì chuyển trạng thái rồi in ra thông báo
                            session.setStatus(AuctionStatus.FINISHED);
                            System.out.println("==> [HỆ THỐNG]: Phiên " + session.getAuctionID() + " đã kết thúc tự động!");
                        }
                    } catch (Exception e) {
                    }
                }
            }
        }, 0, 1, TimeUnit.SECONDS);
    }


    public boolean createSession(String auctionID, String itemID, String sellerID, double startPrice) { // tạo session mới
        if (sessions.containsKey(auctionID)) { // kiểm tra bằng auctionID
            return false;
        }

        AuctionSession newSession = new AuctionSession(auctionID, itemID, sellerID, startPrice); // khởi tạo với 4 thông tin
        sessions.put(auctionID, newSession);
        return true;
    }

    public boolean startSession(String auctionID) {
        AuctionSession session = sessions.get(auctionID);
        if (session == null || session.getStatus() != AuctionStatus.OPEN) {
            return false;
        }

        session.setStatus(AuctionStatus.RUNNING);
        return true;
    }
    // tìm phiên theo Id, nếu thấy và đang ở trạng thái OPEN, chuyển sang RUNNING

    public boolean placeBid(String auctionID, String bidderID, double bidAmount) {
        AuctionSession session = sessions.get(auctionID);
        if (session == null) {
            return false;
        }

        return session.processBid(bidderID, bidAmount);
    }

    // tìm phiên đấu giá tương ứng rồi ủy quyền cho AuctionSession xử lý
    public boolean closeSession(String auctionID) {
        AuctionSession session = sessions.get(auctionID);
        if (session == null) {
            return false;
        }

        AuctionStatus status = session.getStatus();
        if (status != AuctionStatus.RUNNING && status != AuctionStatus.EXTENDED) {
            return false;
        }

        session.setStatus(AuctionStatus.FINISHED);
        return true;
    }

    // đóng phiên thủ công

    public AuctionSession getSession(String auctionID) {
        return sessions.get(auctionID);
    }

    public List<AuctionSession> getAllSessions() {
        return Collections.unmodifiableList(new ArrayList<>(sessions.values()));
    }
    // truy xuất dữ liệu
}