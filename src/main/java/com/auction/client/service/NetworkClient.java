package com.auction.client.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class NetworkClient {
    private static NetworkClient instance;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    // FIX: Thêm queue để tách luồng listener và sendRequest
    private final LinkedBlockingQueue<String> responseQueue = new LinkedBlockingQueue<>();
    private volatile boolean isListening = false;

    private NetworkClient() {}

    public static synchronized NetworkClient getInstance() {
        if (instance == null) {
            instance = new NetworkClient();
        }
        return instance;
    }

    public boolean connect(String host, int port) {
        if (socket != null && !socket.isClosed()) {
            return true;
        }
        try {
            socket = new Socket();
            socket.connect(new java.net.InetSocketAddress(host, port), 3000);
            // FIX: Bỏ setSoTimeout vì listener thread cần block vô hạn.
            // Timeout cho sendRequest được xử lý bằng responseQueue.poll(5s)
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String welcome = in.readLine();
            System.out.println("[CLIENT] Đã kết nối: " + welcome);
            return true;
        } catch (IOException e) {
            System.err.println("[CLIENT] Lỗi kết nối Server: " + e.getMessage());
            return false;
        }
    }

    /**
     * Gửi request và đợi response.
     * - Nếu listener đang chạy: response sẽ được listener bỏ vào queue, sendRequest lấy từ queue.
     * - Nếu listener chưa chạy (login/register): đọc thẳng từ stream như cũ.
     */
    public synchronized String sendRequest(String request) {
        if (socket == null || socket.isClosed()) {
            if (!connect("localhost", 9999)) {
                return null;
            }
        }
        try {
            out.println(request);

            if (isListening) {
                // Listener đang chạy → đợi tối đa 5 giây từ queue
                String response = responseQueue.poll(5, TimeUnit.SECONDS);
                if (response == null) {
                    System.err.println("[CLIENT] Timeout chờ response cho: " + request);
                }
                return response;
            } else {
                // Chưa có listener → đọc trực tiếp (dùng cho login/register)
                socket.setSoTimeout(5000);
                String response = in.readLine();
                socket.setSoTimeout(0);
                return response;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (java.net.SocketTimeoutException e) {
            System.err.println("[CLIENT] Server không phản hồi cho: " + request);
            return null;
        } catch (IOException e) {
            System.err.println("[CLIENT] Lỗi mạng: " + e.getMessage());
            disconnect();
            return null;
        }
    }

    public void disconnect() {
        try {
            isListening = false;
            if (out != null) out.println("QUIT");
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.err.println("Lỗi đóng kết nối: " + e.getMessage());
        }
    }

    public interface MessageListener {
        void onMessageReceived(String message);
    }

    private MessageListener listener;

    public void setListener(MessageListener listener) {
        this.listener = listener;
    }

    /**
     * Bắt đầu thread lắng nghe server push.
     * - Tin nhắn "CAP_NHAT|..." là broadcast → đẩy lên listener (UI).
     * - Mọi tin khác là response cho sendRequest → bỏ vào responseQueue.
     */
    public void startListening() {
        if (isListening) return; // Tránh khởi động nhiều lần
        isListening = true;

        new Thread(() -> {
            try {
                while (isListening) {
                    String response = in.readLine();
                    if (response == null) {
                        System.err.println("[CLIENT] Server đóng kết nối.");
                        break;
                    }

                    // Server broadcast (push chủ động) → cập nhật UI
                    if (response.startsWith("CAP_NHAT|")) {
                        if (listener != null) {
                            listener.onMessageReceived(response);
                        }
                    } else {
                        // Response cho lệnh sendRequest → bỏ vào queue
                        responseQueue.offer(response);
                    }
                }
            } catch (IOException e) {
                if (isListening) {
                    System.err.println("[CLIENT] Mất kết nối với Server!");
                }
            } finally {
                isListening = false;
            }
        }, "NetworkListener-Thread").start();
    }
}