package com.auction.server.dao;

import com.auction.domain.AuctionSession;
import com.auction.domain.AuctionStatus;
import com.auction.domain.BidTransaction;
import com.auction.server.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AuctionDAO {

    public void saveSession(AuctionSession session) throws SQLException {
        String sql = "INSERT INTO auction_sessions (auction_id, item_id, seller_id, start_time, end_time, status, winner_id, current_highest_bid) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE start_time=?, end_time=?, status=?, winner_id=?, current_highest_bid=?";
        
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        try (Connection conn = DatabaseUtil.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, session.getAuctionID());
            stmt.setString(2, session.getItemID());
            stmt.setString(3, session.getSellerID());
            
            // Xử lý startTime là String trong Session, chuyển đổi cẩn thận
            stmt.setString(4, session.getStartTime()); 
            stmt.setString(5, session.getEndTime());
            stmt.setString(6, session.getStatus().name());
            stmt.setString(7, session.getWinnerID());
            stmt.setDouble(8, session.getCurrentHighestBid());

            stmt.setString(9, session.getStartTime());
            stmt.setString(10, session.getEndTime());
            stmt.setString(11, session.getStatus().name());
            stmt.setString(12, session.getWinnerID());
            stmt.setDouble(13, session.getCurrentHighestBid());

            stmt.executeUpdate();
        }
    }

    public void saveBidTransaction(BidTransaction tx) throws SQLException {
        String sql = "INSERT INTO bid_transactions (auction_id, bidder_id, bid_amount, bid_time) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tx.getAuctionID());
            stmt.setString(2, tx.getBidderID());
            stmt.setDouble(3, tx.getBidAmount());
            stmt.setString(4, tx.getBidTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            stmt.executeUpdate();
        }
    }
}
