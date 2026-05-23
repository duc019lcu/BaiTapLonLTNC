package com.auction.client.controller;

import com.auction.client.service.NetworkClient;
import com.auction.client.viewmodel.AuctionRow;
import com.auction.common.models.Bidder;
import com.auction.common.models.User;
import com.auction.common.models.UserManager;
import com.auction.common.util.SceneUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Callback;

public class MainAuctionController {

    @FXML private Label lblWelcome;
    @FXML private Label lblBalance;       // Hiển thị số dư Bidder
    @FXML private TableView<AuctionRow> auctionTable;
    @FXML private Button btnCreateAuction;
    @FXML private Button btnRefresh;
    @FXML private Button btnDeposit;      // Nút nạp tiền (chỉ Bidder)

    private final ObservableList<AuctionRow> auctionData = FXCollections.observableArrayList();
    private AuctionRow selectedAuction;

    @FXML
    public void initialize() {
        try {
            User currentUser = UserManager.getInstance().getCurrentUser();
            if (currentUser != null) {
                lblWelcome.setText("Xin chào, " + currentUser.getFullName()
                        + " | Vai trò: " + currentUser.getClass().getSimpleName());

                if (currentUser instanceof Bidder) {
                    // Hiện số dư và nút nạp tiền cho Bidder
                    updateBalanceDisplay((Bidder) currentUser);
                    btnDeposit.setVisible(true);
                    btnDeposit.setManaged(true);
                } else {
                    // Ẩn số dư và nút nạp tiền với Seller/Admin
                    lblBalance.setVisible(false);
                    lblBalance.setManaged(false);
                }

                // Chỉ Seller mới tạo được phiên đấu giá
                if (!currentUser.getClass().getSimpleName().equalsIgnoreCase("Seller")) {
                    btnCreateAuction.setVisible(false);
                    btnCreateAuction.setManaged(false);
                }
            }

            // =========================================================================
            // FIX: Cấu hình định dạng hiển thị cho cột Giá hiện tại (Cột số 3 trong bảng)
            // =========================================================================
            if (!auctionTable.getColumns().isEmpty()) {
                // Tìm đúng cột "Giá hiện tại (VNĐ)" (ở đây là cột thứ 3, index 3 theo FXML của bạn)
                TableColumn<AuctionRow, Double> priceColumn = (TableColumn<AuctionRow, Double>) auctionTable.getColumns().get(3);

                priceColumn.setCellFactory(new Callback<TableColumn<AuctionRow, Double>, TableCell<AuctionRow, Double>>() {
                    @Override
                    public TableCell<AuctionRow, Double> call(TableColumn<AuctionRow, Double> param) {
                        return new TableCell<AuctionRow, Double>() {
                            @Override
                            protected void updateItem(Double item, boolean empty) {
                                super.updateItem(item, empty);
                                if (empty || item == null) {
                                    setText(null);
                                } else {
                                    // Định dạng số double thành dạng có dấu phân cách, ví dụ: 36,000,000
                                    setText(String.format("%,.0f", item));
                                }
                            }
                        };
                    }
                });
            }

            auctionTable.setItems(auctionData);
            auctionTable.setOnMouseClicked(e ->
                    selectedAuction = auctionTable.getSelectionModel().getSelectedItem());

            loadAuctionDataAsync();
        } catch (Exception e) {
            System.err.println("[ERROR] Initialize lỗi: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** Cập nhật nhãn số dư trên header */
    private void updateBalanceDisplay(Bidder bidder) {
        double balance = bidder.getBalance();
        lblBalance.setText(String.format("Số dư: %,.0f VNĐ", balance));
        // Đổi màu nếu số dư thấp (< 1 triệu)
        if (balance < 1_000_000) {
            lblBalance.setStyle("-fx-text-fill: #f87171; -fx-font-weight: bold;");
        } else {
            lblBalance.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
        }
    }

    // =========================================================================
    // NẠP TIỀN — Hiện dialog nhập số tiền, gửi DEPOSIT lên server
    // =========================================================================
    @FXML
    void handleDeposit(ActionEvent event) {
        User currentUser = UserManager.getInstance().getCurrentUser();
        if (!(currentUser instanceof Bidder)) return;

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nạp tiền vào ví");
        dialog.setHeaderText("Nhập số tiền muốn nạp (VNĐ)");
        dialog.setContentText("Số tiền:");
        dialog.getDialogPane().setStyle("-fx-background-color: #1e293b;");

        dialog.showAndWait().ifPresent(input -> {
            String raw = input.trim().replaceAll(",", "");
            if (raw.isEmpty()) return;

            double amount;
            try {
                amount = Double.parseDouble(raw);
                if (amount <= 0) {
                    showAlert(Alert.AlertType.WARNING, "Lỗi", "Số tiền phải lớn hơn 0!");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi nhập liệu", "Vui lòng nhập số hợp lệ!");
                return;
            }

            final double finalAmount = amount;

            // Gửi request trong background thread
            new Thread(() -> {
                String response = NetworkClient.getInstance().sendRequest(
                        "DEPOSIT|" + currentUser.getId() + "|" + (long) finalAmount);

                Platform.runLater(() -> {
                    if (response == null) {
                        showAlert(Alert.AlertType.ERROR, "Lỗi mạng", "Máy chủ không phản hồi!");
                        return;
                    }
                    if (response.startsWith("DEPOSIT_SUCCESS|")) {
                        // Cập nhật wallet trong RAM
                        Bidder bidder = (Bidder) currentUser;
                        bidder.getWallet().deposit(finalAmount);

                        // Cập nhật label số dư
                        updateBalanceDisplay(bidder);

                        String newBalance = response.split("\\|")[1];
                        showAlert(Alert.AlertType.INFORMATION, "Nạp tiền thành công!",
                                String.format("Đã nạp %,.0f VNĐ vào tài khoản.\nSố dư hiện tại: %s VNĐ",
                                        finalAmount, formatNumber(newBalance)));
                    } else {
                        String reason = response.contains("|") ? response.split("\\|", 2)[1] : response;
                        showAlert(Alert.AlertType.ERROR, "Nạp tiền thất bại!", reason);
                    }
                });
            }).start();
        });
    }

    /** Format chuỗi số thành dạng có dấu phẩy ngăn cách */
    private String formatNumber(String numStr) {
        try {
            long val = Long.parseLong(numStr.trim());
            return String.format("%,d", val);
        } catch (Exception e) {
            return numStr;
        }
    }

    // =========================================================================
    // Load danh sách phiên đấu giá
    // =========================================================================
    private void loadAuctionDataAsync() {
        new Thread(() -> {
            try {
                loadAuctionData();
            } catch (Exception e) {
                System.err.println("[ERROR] Load auction data lỗi: " + e.getMessage());
                Platform.runLater(() ->
                        showAlert(Alert.AlertType.WARNING, "Lỗi tải dữ liệu",
                                "Không thể tải danh sách phiên: " + e.getMessage()));
            }
        }).start();
    }

    private void loadAuctionData() {
        String response = NetworkClient.getInstance().sendRequest("LIST");
        if (response == null || !response.startsWith("DANH_SACH")) {
            Platform.runLater(() -> {
                auctionData.clear();
                showAlert(Alert.AlertType.WARNING, "Không tải được dữ liệu",
                        "Không thể lấy danh sách phiên từ máy chủ.");
            });
            return;
        }

        java.util.List<AuctionRow> tempRows = new java.util.ArrayList<>();
        String[] entries = response.split("\\|");
        int stt = 1;

        for (int i = 1; i < entries.length; i++) {
            if ("trong".equalsIgnoreCase(entries[i])) break;
            String[] parts = entries[i].split(":");
            if (parts.length != 3) continue;

            String auctionId   = parts[0];
            double currentPrice = parseDouble(parts[1]);
            String status      = parts[2];

            // Lấy chi tiết từng phiên
            String detailResponse = NetworkClient.getInstance().sendRequest("GET_SESSION|" + auctionId);
            String itemName     = auctionId;
            String timeRemaining = "00:00:00";

            if (detailResponse != null && detailResponse.startsWith("PHIEN|")) {
                for (String detail : detailResponse.split("\\|")) {
                    String[] kv = detail.split("=", 2);
                    if (kv.length == 2) {
                        if ("vat_pham".equals(kv[0])) itemName = kv[1];
                        else if ("end_time".equals(kv[0])) timeRemaining = calculateTimeRemaining(kv[1]);
                    }
                }
            }
            tempRows.add(new AuctionRow(stt++, auctionId, itemName, currentPrice, 0, status, timeRemaining));
        }

        Platform.runLater(() -> {
            auctionData.clear();
            auctionData.addAll(tempRows);
        });
    }

    private double parseDouble(String value) {
        if (value == null || value.isBlank()) return 0.0;
        try {
            return Double.parseDouble(value.replaceAll("[^\\d.]", ""));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private String calculateTimeRemaining(String endTimeStr) {
        try {
            java.time.LocalDateTime endTime = java.time.LocalDateTime.parse(endTimeStr);
            java.time.Duration remaining = java.time.Duration.between(java.time.LocalDateTime.now(), endTime);
            long seconds = remaining.getSeconds();
            if (seconds <= 0) return "Đã kết thúc";
            return String.format("%02d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, seconds % 60);
        } catch (Exception e) {
            return "00:00:00";
        }
    }

    // =========================================================================
    // Các action khác
    // =========================================================================
    @FXML
    void handleLogout(ActionEvent event) {
        UserManager.getInstance().setCurrentUser(null);
        SceneUtil.changeScene(event, "Login.fxml", "Đăng nhập");
    }

    @FXML
    void handleCreateAuction(ActionEvent event) {
        SceneUtil.changeScene(event, "CreateAuction.fxml", "Tạo phiên đấu giá mới");
    }

    @FXML
    void handleJoinAuction(ActionEvent event) {
        if (selectedAuction != null) {
            AuctionRoomController.setSelectedAuction(selectedAuction.getAuctionId(), selectedAuction.getItemName());
            SceneUtil.changeScene(event, "AuctionRoom.fxml", "Phòng đấu giá: " + selectedAuction.getItemName());
        } else {
            showAlert(Alert.AlertType.INFORMATION, "Chưa chọn phiên",
                    "Vui lòng chọn một phiên đấu giá để tham gia!");
        }
    }

    @FXML
    void handleRefresh(ActionEvent event) {
        loadAuctionDataAsync();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}