package com.auction.server.network;

import com.auction.domain.AuctionManager;
import com.auction.server.util.DatabaseUtil;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuctionServer {
    public static final int DEFAULT_PORT = 9999;

    private final int port;
    private final Set<ClientHandler> clients = Collections.synchronizedSet(new HashSet<>());
    private final ExecutorService threadPool = Executors.newFixedThreadPool(50);

    public AuctionServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        initializeDatabase();  // Khởi tạo database
        seedDemoData();
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("[MÁY CHỦ] Đang lắng nghe ở cổng " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[MÁY CHỦ] Client kết nối: " + clientSocket.getRemoteSocketAddress());
                ClientHandler handler = new ClientHandler(clientSocket, this);
                clients.add(handler);
                threadPool.execute(handler);
            }
        }
    }

    public void broadcast(String message) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                client.sendMessage(message);
            }
        }
    }

    public void removeClient(ClientHandler handler) {
        clients.remove(handler);
    }

    private void seedDemoData() {
        AuctionManager manager = AuctionManager.getInstance();
        if (manager.createSession("A1", "ITEM1", "SELLER1", 500.0)) {
            manager.startSession("A1");
            System.out.println("[MÁY CHỦ] Đã tạo phiên mẫu A1.");
        }
    }

    private void initializeDatabase() {
        try {
            Connection conn = DatabaseUtil.getInstance().getConnection();
            
            // Lưu ý: Câu lệnh USE không được thực thi trực tiếp, phải tách thành connection riêng
            Statement stmt = conn.createStatement();
            
            // Tạo database
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS auction_system");
            
            // Đóng statement cũ
            stmt.close();
            
            // Tạo connection mới với database auction_system
            String dbUrl = "jdbc:mysql://localhost:3306/auction_system";
            String user = "root";  // Từ config
            String pass = "";      // Từ config
            
            try {
                // Lấy từ properties
                java.util.Properties props = new java.util.Properties();
                java.io.InputStream in = getClass().getClassLoader().getResourceAsStream("config/application.properties");
                if (in != null) {
                    props.load(in);
                    String fullUrl = props.getProperty("db.url");
                    user = props.getProperty("db.username");
                    pass = props.getProperty("db.password");
                    
                    if (fullUrl.contains("auction_system")) {
                        dbUrl = fullUrl;
                    } else {
                        dbUrl = fullUrl.replace(":3306/", ":3306/auction_system");
                    }
                }
            } catch (Exception e) {
                // Nếu không tìm được config, dùng mặc định
                System.out.println("[CẢNH BÁO] Không tìm được config, dùng default");
            }
            
            java.sql.Connection dbConn = java.sql.DriverManager.getConnection(dbUrl, user, pass);
            Statement dbStmt = dbConn.createStatement();
            
            // Tạo bảng users
            dbStmt.executeUpdate("CREATE TABLE IF NOT EXISTS users (" +
                "id VARCHAR(50) PRIMARY KEY," +
                "username VARCHAR(50) NOT NULL UNIQUE," +
                "password VARCHAR(255) NOT NULL," +
                "full_name VARCHAR(100) NOT NULL," +
                "email VARCHAR(100) NOT NULL UNIQUE," +
                "role VARCHAR(20) NOT NULL," +
                "balance DOUBLE DEFAULT 0" +
            ")");
            
            // Tạo bảng items
            dbStmt.executeUpdate("CREATE TABLE IF NOT EXISTS items (" +
                "id VARCHAR(50) PRIMARY KEY," +
                "name VARCHAR(100) NOT NULL," +
                "description TEXT," +
                "init_price DOUBLE NOT NULL," +
                "category VARCHAR(50) NOT NULL" +
            ")");
            
            // Tạo bảng auction_sessions
            dbStmt.executeUpdate("CREATE TABLE IF NOT EXISTS auction_sessions (" +
                "auction_id VARCHAR(50) PRIMARY KEY," +
                "item_id VARCHAR(50) NOT NULL," +
                "seller_id VARCHAR(50) NOT NULL," +
                "start_time DATETIME NOT NULL," +
                "end_time DATETIME NOT NULL," +
                "status VARCHAR(20) NOT NULL," +
                "winner_id VARCHAR(50)," +
                "current_highest_bid DOUBLE DEFAULT 0," +
                "FOREIGN KEY (item_id) REFERENCES items(id)," +
                "FOREIGN KEY (seller_id) REFERENCES users(id)," +
                "FOREIGN KEY (winner_id) REFERENCES users(id)" +
            ")");
            
            // Tạo bảng bid_transactions
            dbStmt.executeUpdate("CREATE TABLE IF NOT EXISTS bid_transactions (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "auction_id VARCHAR(50) NOT NULL," +
                "bidder_id VARCHAR(50) NOT NULL," +
                "bid_amount DOUBLE NOT NULL," +
                "bid_time DATETIME NOT NULL," +
                "FOREIGN KEY (auction_id) REFERENCES auction_sessions(auction_id)," +
                "FOREIGN KEY (bidder_id) REFERENCES users(id)" +
            ")");
            
            dbStmt.close();
            // KHÔNG đóng dbConn - giữ connection sống cho quá trình chạy app
            System.out.println("[MÁY CHỦ] ✓ Database đã khởi tạo thành công");
        } catch (Exception e) {
            System.err.println("[MÁY CHỦ] ✗ Lỗi khởi tạo database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        try {
            new AuctionServer(port).start();
        } catch (IOException e) {
            System.err.println("[MÁY CHỦ] Lỗi khi khởi động: " + e.getMessage());
        }
    }
}