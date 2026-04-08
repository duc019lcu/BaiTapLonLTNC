public class Main {
    public static void main(String[] args) {
        AuctionSession session = new AuctionSession("001", "LAPTOP", "TRANDUC", 500.0);
        
        // Giả sử mở phiên đấu giá
        session.setStatus(AuctionStatus.RUNNING);
        System.out.println("--- Bắt đầu đấu giá ---");

        // Lần 1: Người A đặt 520 (Hợp lệ)
        session.processBid("User_A", 520.0);

        // Lần 2: Người B đặt 510 (Lỗi vì thấp hơn 520)
        session.processBid("User_B", 510.0);

        // Lần 3: Người C đặt 600 (Hợp lệ)
        session.processBid("User_C", 600.0);
}
    
}
