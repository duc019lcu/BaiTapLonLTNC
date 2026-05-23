package com.auction.server.network;

import com.auction.domain.AuctionManager;
import com.auction.domain.AuctionSession;
import com.auction.server.dao.ItemDAO;
import com.auction.server.dao.UserDAO;
import com.auction.common.models.Bidder;
import com.auction.common.models.Item;
import com.auction.common.models.Electronics;
import com.auction.common.models.User;

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

    public void sendMessage(String message) {
        if (out != null) out.println(message);
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
        if (raw == null || raw.isBlank()) return "LOI|Yeu cau rong";
        String[] parts = raw.split("\\|");
        if (parts.length == 0 || parts[0].isBlank()) return "LOI|Khong co lenh";
        String command = parts[0].toUpperCase();
        AuctionManager manager = AuctionManager.getInstance();
        switch (command) {
            case "LOGIN":          return processLogin(parts);
            case "REGISTER":       return processRegister(parts);
            case "DEPOSIT":        return processDeposit(parts);
            case "PLACE_BID":      return processBid(parts, manager);
            case "LIST":           return buildListResponse(manager);
            case "GET_SESSION":    return buildSessionResponse(parts, manager);
            case "CREATE_AUCTION": return processCreateAuction(parts, manager);
            case "CREATE_ITEM":    return processCreateItem(parts);
            case "QUIT":           return "TAM_BIET";
            default:               return "LOI|Lenh khong hop le";
        }
    }

    private String fmt(double price) {
        return String.format("%.0f", price);
    }

    private String processLogin(String[] parts) {
        if (parts.length != 3) return "LOI|Dinh dang: LOGIN|username|password";
        try {
            UserDAO userDAO = new UserDAO();
            User user = userDAO.getUserByUsername(parts[1]);
            if (user != null && user.getPassword().equals(parts[2])) {
                String balancePart = (user instanceof Bidder)
                        ? "|" + fmt(((Bidder) user).getBalance()) : "";
                return "LOGIN_SUCCESS|" + user.getRole() + "|" + user.getId()
                        + "|" + user.getFullName() + "|" + user.getEmail() + balancePart;
            }
            return "LOGIN_FAILED|Sai tai khoan hoac mat khau";
        } catch (Exception e) {
            return "LOI|Loi he thong: " + e.getMessage();
        }
    }

    private String processRegister(String[] parts) {
        if (parts.length != 5) return "LOI|Dinh dang: REGISTER|username|password|email|role";
        try {
            UserDAO userDAO = new UserDAO();
            if (userDAO.getUserByUsername(parts[1]) != null)
                return "REGISTER_FAILED|Tên đăng nhập đã tồn tại!";

            String id = String.valueOf(System.currentTimeMillis());
            User newUser;

            if ("SELLER".equals(parts[4])) {
                newUser = new com.auction.common.models.Seller(id, parts[1], parts[2], parts[1], parts[3]);
            } else {
                newUser = new Bidder(id, parts[1], parts[2], parts[1], parts[3], 0.0);
            }

            userDAO.saveUser(newUser);
            return "REGISTER_SUCCESS";
        } catch (Exception e) {
            return "LOI|Loi DB: " + e.getMessage();
        }
    }

    private String processDeposit(String[] parts) {
        if (parts.length != 3) return "DEPOSIT_FAILED|Dinh dang sai";
        double amount;
        try {
            amount = Double.parseDouble(parts[2]);
            if (amount <= 0) return "DEPOSIT_FAILED|So tien nap phai lon hon 0 VND!";
            if (amount > 10_000_000_000.0) return "DEPOSIT_FAILED|So tien nap toi da moi lan la 10 ty VND!";
        } catch (NumberFormatException e) {
            return "DEPOSIT_FAILED|So tien khong hop le!";
        }
        try {
            UserDAO userDAO = new UserDAO();
            User user = userDAO.findById(parts[1]);
            if (user == null) return "DEPOSIT_FAILED|Khong tim thay tai khoan!";
            if (!(user instanceof Bidder)) return "DEPOSIT_FAILED|Chi Bidder moi co the nap tien!";
            Bidder bidder = (Bidder) user;
            bidder.getWallet().deposit(amount);
            userDAO.saveUser(bidder);
            System.out.println("[SERVER] Bidder " + parts[1] + " nap " + fmt(amount)
                    + " VND. So du moi: " + fmt(bidder.getBalance()));
            return "DEPOSIT_SUCCESS|" + fmt(bidder.getBalance());
        } catch (Exception e) {
            return "DEPOSIT_FAILED|Loi he thong: " + e.getMessage();
        }
    }

    private String processBid(String[] parts, AuctionManager manager) {
        if (parts.length != 4) return "LOI|Dinh dang: PLACE_BID|auctionId|bidderId|amount";

        String auctionId = parts[1];
        String bidderId  = parts[2];
        double amount;
        try {
            amount = Double.parseDouble(parts[3]);
        } catch (NumberFormatException e) {
            return "LOI|So tien dat gia phai la so";
        }

        // KIỂM TRA PHÂN QUYỀN - CHỈ CHO PHÉP BIDDER ĐẶT GIÁ
        try {
            UserDAO userDAO = new UserDAO();
            User user = userDAO.findById(bidderId);

            if (user == null) {
                return "TU_CHOI|Không tìm thấy tài khoản người dùng!";
            }

            if (!"BIDDER".equalsIgnoreCase(user.getRole())) {
                return "TU_CHOI|Bị từ chối! Chỉ tài khoản người mua (Bidder) mới có quyền tham gia đặt giá.";
            }
        } catch (Exception e) {
            return "LOI|Lỗi hệ thống khi xác thực quyền: " + e.getMessage();
        }

        AuctionSession session = manager.getSession(auctionId);
        if (session == null) return "LOI|Khong tim thay phien dau gia";

        boolean success = manager.placeBid(auctionId, bidderId, amount);
        if (success) {
            session = manager.getSession(auctionId);
            server.broadcast("CAP_NHAT|" + auctionId
                    + "|" + fmt(session.getCurrentHighestBid())
                    + "|" + session.getWinnerID()
                    + "|" + session.getStatus());
            return "CHAP_NHAN|gia_hien_tai=" + fmt(session.getCurrentHighestBid())
                    + "|nguoi_dan_dau=" + session.getWinnerID()
                    + "|trang_thai=" + session.getStatus();
        }

        String reason;
        if ("FINISHED".equals(session.getStatus().name())) {
            reason = "Phien dau gia nay da ket thuc!";
        } else if (amount <= session.getCurrentHighestBid()) {
            reason = "Gia dat phai cao hon gia hien tai ("
                    + fmt(session.getCurrentHighestBid()) + " VND)!";
        } else {
            reason = "So du tài khoản không đủ để đặt cọc " + fmt(amount)
                    + " VND! Vui lòng nạp thêm tiền.";
        }
        return "TU_CHOI|" + reason;
    }

    private String processCreateAuction(String[] parts, AuctionManager manager) {
        if (parts.length != 7)
            return "LOI|Dinh dang: CREATE_AUCTION|auctionId|itemId|itemName|sellerId|startPrice|durationMinutes";
        double startPrice;
        int durationMinutes;
        try {
            startPrice = Double.parseDouble(parts[5]);
            durationMinutes = Integer.parseInt(parts[6]);
        } catch (NumberFormatException e) {
            return "LOI|Gia va thoi gian phai la so";
        }
        try {
            ItemDAO itemDAO = new ItemDAO();
            if (itemDAO.findById(parts[2]) == null)
                itemDAO.saveItem(new Electronics(parts[2], parts[3], "", startPrice, "", "", "", ""));
        } catch (Exception e) {
            System.err.println("[SERVER] Loi luu item: " + e.getMessage());
        }
        boolean created = manager.createSession(parts[1], parts[2], parts[3], parts[4], startPrice, durationMinutes);
        if (!created) return "LOI|Ma phien da ton tai: " + parts[1];
        manager.startSession(parts[1]);
        return "CREATE_AUCTION_SUCCESS|" + parts[1];
    }

    private String processCreateItem(String[] parts) {
        if (parts.length != 6) return "LOI|Dinh dang: CREATE_ITEM|itemId|itemName|description|initPrice|category";
        try {
            double price = Double.parseDouble(parts[4]);
            new ItemDAO().saveItem(new Electronics(parts[1], parts[2], parts[3], price, parts[5], "", "", ""));
            return "CREATE_ITEM_SUCCESS|" + parts[1];
        } catch (Exception e) {
            return "LOI|Loi tao vat pham: " + e.getMessage();
        }
    }

    private String buildListResponse(AuctionManager manager) {
        StringBuilder sb = new StringBuilder("DANH_SACH");
        for (AuctionSession s : manager.getAllSessions()) {
            sb.append("|").append(s.getAuctionID())
                    .append(":").append(fmt(s.getCurrentHighestBid()))
                    .append(":").append(s.getStatus());
        }
        return "DANH_SACH".contentEquals(sb) ? "DANH_SACH|trong" : sb.toString();
    }

    private String buildSessionResponse(String[] parts, AuctionManager manager) {
        if (parts.length != 2) return "LOI|Dinh dang: GET_SESSION|auctionId";
        AuctionSession session = manager.getSession(parts[1]);
        if (session == null) return "LOI|Khong tim thay phien dau gia";
        String itemName = session.getDisplayItem();
        try {
            Item item = new ItemDAO().findById(session.getItemID());
            if (item != null) itemName = item.getName();
        } catch (Exception ignored) {}
        return "PHIEN|id=" + session.getAuctionID()
                + "|vat_pham=" + itemName
                + "|gia_hien_tai=" + fmt(session.getCurrentHighestBid())
                + "|nguoi_dan_dau=" + (session.getWinnerID() == null ? "" : session.getWinnerID())
                + "|trang_thai=" + session.getStatus()
                + "|end_time=" + session.getEndTime();
    }

    private void cleanup() {
        server.removeClient(this);
        try { socket.close(); } catch (IOException ignored) {}
    }
}