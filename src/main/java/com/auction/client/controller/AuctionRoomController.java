package com.auction.client.controller;

import com.auction.client.service.NetworkClient;
import com.auction.common.models.UserManager;
import com.auction.common.util.SceneUtil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class AuctionRoomController {

    private static String selectedAuctionId;
    private static String selectedAuctionItem;

    @FXML
    private Label lblItemName;

    @FXML
    private Label lblDescription;

    @FXML
    private Label lblCurrentPrice;

    @FXML
    private Label lblTimer;

    @FXML
    private Label lblWinner;

    @FXML
    private TextField txtBidAmount;

    @FXML
    private LineChart<String, Number> bidChart;

    private XYChart.Series<String, Number> series;
    
    private double currentPrice = 5000000;
    private int secondsRemaining = 3600; // Mô phỏng 1 tiếng
    private Timer timer;

    public static void setSelectedAuction(String auctionId, String auctionItem) {
        selectedAuctionId = auctionId;
        selectedAuctionItem = auctionItem;
    }

    @FXML
    public void initialize() {
        // Khởi tạo biểu đồ lịch sử giá (Tính năng nâng cao)
        series = new XYChart.Series<>();
        series.setName("Lịch sử giá");
        bidChart.getData().add(series);

        String itemName = selectedAuctionItem != null ? selectedAuctionItem : "Tranh phong cảnh cổ điển";
        lblItemName.setText(itemName);
        lblDescription.setText("Phiên đấu giá: " + (selectedAuctionId != null ? selectedAuctionId : "#0001") + "\nSản phẩm: " + itemName);

        if (selectedAuctionId != null) {
            loadAuctionDataFromServer(selectedAuctionId);
        } else {
            updatePriceDisplay(currentPrice, "Chưa có");
            addChartData(currentPrice);
            startTimer();
        }
    }

    private void loadAuctionDataFromServer(String auctionId) {
        String response = NetworkClient.getInstance().sendRequest("GET_SESSION|" + auctionId);
        if (!response.startsWith("PHIEN|")) {
            showAlert("Lỗi tải phiên", "Không thể lấy dữ liệu phiên đấu giá từ máy chủ.");
            updatePriceDisplay(currentPrice, "Chưa có");
            addChartData(currentPrice);
            startTimer();
            return;
        }

        Map<String, String> sessionData = parseKeyValueResponse(response);
        currentPrice = parseDouble(sessionData.getOrDefault("gia_hien_tai", String.valueOf(currentPrice)), currentPrice);
        String winner = sessionData.getOrDefault("nguoi_dan_dau", "");
        String status = sessionData.getOrDefault("trang_thai", "OPEN");
        String item = sessionData.getOrDefault("vat_pham", selectedAuctionItem != null ? selectedAuctionItem : "Sản phẩm chưa rõ");
        String endTimeStr = sessionData.getOrDefault("end_time", "");

        lblItemName.setText(item);
        lblDescription.setText("Phiên đấu giá: " + auctionId + "\nSản phẩm: " + item + "\nTrạng thái: " + status);
        updatePriceDisplay(currentPrice, winner.isBlank() ? "Chưa có" : winner);
        addChartData(currentPrice);

        if ("FINISHED".equalsIgnoreCase(status)) {
            secondsRemaining = 0;
            lblTimer.setText("ĐÃ KẾT THÚC");
            txtBidAmount.setDisable(true);
        } else {
            secondsRemaining = parseSecondsUntil(endTimeStr, 3600);
            if (secondsRemaining <= 0) {
                secondsRemaining = 0;
                lblTimer.setText("ĐÃ KẾT THÚC");
                txtBidAmount.setDisable(true);
            } else {
                startTimer();
            }
        }
    }

    private Map<String, String> parseKeyValueResponse(String response) {
        Map<String, String> data = new HashMap<>();
        String[] parts = response.split("\\|");
        for (int i = 1; i < parts.length; i++) {
            String[] kv = parts[i].split("=", 2);
            if (kv.length == 2) {
                data.put(kv[0], kv[1]);
            }
        }
        return data;
    }

    private int parseSecondsUntil(String endTimeStr, int defaultSeconds) {
        if (endTimeStr == null || endTimeStr.isBlank()) {
            return defaultSeconds;
        }
        try {
            LocalDateTime endTime = LocalDateTime.parse(endTimeStr);
            Duration remaining = Duration.between(LocalDateTime.now(), endTime);
            return (int) Math.max(0, remaining.getSeconds());
        } catch (Exception e) {
            return defaultSeconds;
        }
    }

    private double parseDouble(String raw, double defaultValue) {
        try {
            return Double.parseDouble(raw);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private void startTimer() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (secondsRemaining > 0) {
                    secondsRemaining--;
                    Platform.runLater(() -> {
                        int hours = secondsRemaining / 3600;
                        int minutes = (secondsRemaining % 3600) / 60;
                        int secs = secondsRemaining % 60;
                        lblTimer.setText(String.format("%02d:%02d:%02d", hours, minutes, secs));
                        
                        if (secondsRemaining <= 30) {
                            lblTimer.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 36px; -fx-font-weight: bold;"); // Chữ đỏ báo hiệu sắp hết giờ
                        }
                    });
                } else {
                    timer.cancel();
                    Platform.runLater(() -> {
                        lblTimer.setText("ĐÃ KẾT THÚC");
                        txtBidAmount.setDisable(true);
                    });
                }
            }
        }, 1000, 1000);
    }

    @FXML
    void handlePlaceBid(ActionEvent event) {
        if (secondsRemaining <= 0 || selectedAuctionId == null) return;

        try {
            double bidAmount = Double.parseDouble(txtBidAmount.getText());
            if (bidAmount <= currentPrice) {
                showAlert("Giá không hợp lệ", "Bạn phải đặt giá cao hơn giá hiện tại!");
                return;
            }

            String bidderId = UserManager.getInstance().getCurrentUser() != null
                    ? UserManager.getInstance().getCurrentUser().getId()
                    : "anonymous";
            String response = NetworkClient.getInstance().sendRequest(
                    "PLACE_BID|" + selectedAuctionId + "|" + bidderId + "|" + bidAmount);

            if (response.startsWith("CHAP_NHAN|") || response.startsWith("CAP_NHAT|")) {
                Map<String, String> resultData = parseKeyValueResponse(response);
                currentPrice = parseDouble(resultData.getOrDefault("gia_hien_tai", String.valueOf(currentPrice)), currentPrice);
                String winner = resultData.getOrDefault("nguoi_dan_dau", "");
                String status = resultData.getOrDefault("trang_thai", "RUNNING");

                updatePriceDisplay(currentPrice, winner.isBlank() ? "Chưa có" : winner);
                addChartData(currentPrice);

                if ("FINISHED".equalsIgnoreCase(status)) {
                    secondsRemaining = 0;
                    lblTimer.setText("ĐÃ KẾT THÚC");
                    txtBidAmount.setDisable(true);
                }
                showAlert("Đặt giá thành công", "Bạn đã đặt giá thành công!");
                txtBidAmount.clear();
            } else {
                String message = response.contains("|") ? response.split("\\|", 2)[1] : "Đặt giá không thành công.";
                showAlert("Đặt giá thất bại", message);
            }
        } catch (NumberFormatException e) {
            showAlert("Lỗi nhập liệu", "Vui lòng nhập số tiền hợp lệ!");
        }
    }

    private void updatePriceDisplay(double price, String winner) {
        lblCurrentPrice.setText(String.format("%,.0f VNĐ", price));
        lblWinner.setText(winner);
    }

    private void addChartData(double price) {
        String timeStr = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        series.getData().add(new XYChart.Data<>(timeStr, price));
    }

    @FXML
    void handleBack(ActionEvent event) {
        if (timer != null) timer.cancel(); // Dừng đồng hồ khi thoát
        SceneUtil.changeScene(event, "MainAuction.fxml", "Sàn Đấu Giá");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }
}
