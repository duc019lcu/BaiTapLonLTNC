package com.auction.server.network;

import com.auction.domain.AuctionManager;
import com.auction.domain.AuctionSession;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final AuctionServer server;
    private PrintWriter out;

    public ClientHandler(Socket socket, AuctionServer server) {
        this.socket = socket;
        this.server = server;
    }

    // Phương thức để server gọi khi cần gửi tin nhắn chủ động cho client
    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)
        ) {
            this.out = writer;
            out.println("CHAO_MUNG|AuctionServer");

            String line;
            while ((line = in.readLine()) != null) {
                String response = handleRequest(line.trim());
                out.println(response);

                if ("TAM_BIET".equals(response)) break;
            }
        } catch (IOException e) {
            System.out.println("[MÁY CHỦ] Lỗi kết nối client: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    private String handleRequest(String raw) {
        if (raw == null || raw.isBlank()) {
            return "LOI|Yeu cau rong";
        }

        String[] parts = raw.split("\\|");
        if (parts.length == 0 || parts[0].isBlank()) {
            return "LOI|Khong co lenh";
        }

        String command = parts[0].toUpperCase();
        AuctionManager manager = AuctionManager.getInstance();

        switch (command) {
            case "LOGIN":
                return processLogin(parts);
            case "REGISTER":
                return processRegister(parts);
            case "PLACE_BID":
                return processBid(parts, manager);
            case "LIST":
                return buildListResponse(manager);
            case "GET_SESSION":
                return buildSessionResponse(parts, manager);
            case "QUIT":
                return "TAM_BIET";
            default:
                return "LOI|Lenh khong hop le";
        }
    }

    private String processLogin(String[] parts) {
        if (parts.length != 3) {
            return "LOI|Dinh dang: LOGIN|username|password";
        }
        String username = parts[1];
        String password = parts[2];

        try {
            com.auction.server.dao.UserDAO userDAO = new com.auction.server.dao.UserDAO();
            com.auction.common.models.User user = userDAO.getUserByUsername(username);
            if (user != null && user.getPassword().equals(password)) {
                return "LOGIN_SUCCESS|" + user.getRole() + "|" + user.getId() + "|" + user.getFullName() + "|" + user.getEmail();
            } else {
                return "LOGIN_FAILED|Sai tai khoan hoac mat khau";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "LOI|Loi he thong: " + e.getMessage();
        }
    }

    private String processRegister(String[] parts) {
        if (parts.length != 5) return "LOI|Dinh dang: REGISTER|username|password|email|role";
        String username = parts[1];
        String password = parts[2];
        String email = parts[3];
        String role = parts[4];
        
        try {
            com.auction.server.dao.UserDAO userDAO = new com.auction.server.dao.UserDAO();
            if (userDAO.getUserByUsername(username) != null) {
                return "REGISTER_FAILED|Tên đăng nhập đã tồn tại!";
            }
            long id = System.currentTimeMillis();
            com.auction.common.models.User newUser;
            if ("SELLER".equals(role)) {
                newUser = new com.auction.common.models.Seller(String.valueOf(id), username, password, username, email);
            } else {
                newUser = new com.auction.common.models.Bidder(String.valueOf(id), username, password, username, email, 0.0);
            }
            userDAO.saveUser(newUser);
            return "REGISTER_SUCCESS";
        } catch (Exception e) {
            e.printStackTrace();
            return "LOI|Lỗi DB: " + e.getMessage();
        }
    }

    private String processBid(String[] parts, AuctionManager manager) {
        if (parts.length != 4) {
            return "LOI|Dinh dang: PLACE_BID|auctionId|bidderId|amount";
        }

        String auctionId = parts[1];
        String bidderId = parts[2];
        double amount;
        try {
            amount = Double.parseDouble(parts[3]);
        } catch (NumberFormatException e) {
            return "LOI|So tien dat gia phai la so";
        }

        boolean success = manager.placeBid(auctionId, bidderId, amount);
        AuctionSession session = manager.getSession(auctionId);
        if (session == null) {
            return "LOI|Khong tim thay phien dau gia";
        }

        if (success) {
            String updateMsg = "CAP_NHAT|id=" + auctionId
                    + "|gia_hien_tai=" + session.getCurrentHighestBid()
                    + "|nguoi_dan_dau=" + session.getWinnerID()
                    + "|trang_thai=" + session.getStatus();
            server.broadcast(updateMsg);
            return "CHAP_NHAN|gia_hien_tai=" + session.getCurrentHighestBid()
                    + "|nguoi_dan_dau=" + session.getWinnerID()
                    + "|trang_thai=" + session.getStatus();
        }

        return "TU_CHOI|gia_hien_tai=" + session.getCurrentHighestBid()
                + "|nguoi_dan_dau=" + session.getWinnerID()
                + "|trang_thai=" + session.getStatus();
    }

    private String buildListResponse(AuctionManager manager) {
        StringBuilder sb = new StringBuilder("DANH_SACH");
        for (AuctionSession s : manager.getAllSessions()) {
            sb.append("|")
                    .append(s.getAuctionID())
                    .append(":")
                    .append(s.getCurrentHighestBid())
                    .append(":")
                    .append(s.getStatus());
        }
        if ("DANH_SACH".contentEquals(sb)) {
            return "DANH_SACH|trong";
        }
        return sb.toString();
    }

    private String buildSessionResponse(String[] parts, AuctionManager manager) {
        if (parts.length != 2) {
            return "LOI|Dinh dang: GET_SESSION|auctionId";
        }
        AuctionSession session = manager.getSession(parts[1]);
        if (session == null) {
            return "LOI|Khong tim thay phien dau gia";
        }
        return "PHIEN|id=" + session.getAuctionID()
                + "|vat_pham=" + session.getItemID()
                + "|gia_hien_tai=" + session.getCurrentHighestBid()
                + "|nguoi_dan_dau=" + (session.getWinnerID() == null ? "" : session.getWinnerID())
                + "|trang_thai=" + session.getStatus()
                + "|end_time=" + session.getEndTime();
    }

    private void cleanup() {
        server.removeClient(this);
        try {
            socket.close();
        } catch (IOException ignored) {}
    }
}