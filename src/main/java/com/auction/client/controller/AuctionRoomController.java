package com.auction.client.controller;

import com.auction.common.util.SceneUtil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

public class AuctionRoomController {

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

    @FXML
    public void initialize() {
        // Khởi tạo biểu đồ lịch sử giá (Tính năng nâng cao)
        series = new XYChart.Series<>();
        series.setName("Lịch sử giá");
        bidChart.getData().add(series);
        
        // Data giả lập để hiển thị UI
        lblItemName.setText("Tranh phong cảnh cổ điển");
        lblDescription.setText("Danh mục: Nghệ thuật\nNgười bán: Seller_01\nGiá khởi điểm: 5,000,000 VNĐ");
        updatePriceDisplay(currentPrice, "Chưa có");

        // Thêm điểm giá đầu tiên vào biểu đồ
        addChartData(currentPrice);

        startTimer();
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
        if (secondsRemaining <= 0) return;

        try {
            double bidAmount = Double.parseDouble(txtBidAmount.getText());
            if (bidAmount <= currentPrice) {
                showAlert("Giá không hợp lệ", "Bạn phải đặt giá cao hơn giá hiện tại!");
                return;
            }

            // TODO: Gửi Socket request lên Server thay vì tự xử lý
            // Server sẽ Broadcast thông tin bid mới về cho tất cả Client trong phòng
            
            // Tạm thời mô phỏng local realtime
            currentPrice = bidAmount;
            updatePriceDisplay(currentPrice, "Bạn");
            addChartData(currentPrice);
            
            // Mô phỏng Anti-sniping (Gia hạn nếu bid ở 30s cuối)
            if (secondsRemaining <= 30) {
                secondsRemaining += 60;
                lblTimer.setStyle("-fx-text-fill: #e67e22; -fx-font-size: 36px; -fx-font-weight: bold;"); // Chữ cam báo gia hạn
                showAlert("Gia hạn thời gian!", "Phiên đấu giá vừa được tự động cộng thêm 60 giây (Anti-sniping).");
            }
            
            txtBidAmount.clear();

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
