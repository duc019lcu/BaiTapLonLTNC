package com.auction.server.network;

import com.auction.domain.AuctionManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
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

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        try {
            new AuctionServer(port).start();
        } catch (IOException e) {
            System.err.println("[MÁY CHỦ] Lỗi khi khởi động: " + e.getMessage());
        }
    }
}