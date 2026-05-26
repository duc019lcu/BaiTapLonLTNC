package com.auction.client.controller;

import com.auction.client.service.NetworkClient;
import com.auction.client.viewmodel.AuctionRow;
import com.auction.common.models.User;
import com.auction.common.models.UserManager;
import com.auction.common.util.SceneUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Controller màn hình danh sách phiên đấu giá.
 *
 * <p>Dùng lệnh <code>LIST_DETAIL</code> để lấy toàn bộ thông tin phiên
 * trong <strong>một lần gọi mạng duy nhất</strong>, thay vì N+1 calls như trước.</p>
 *
 * <p>Đồng hồ đếm ngược trong bảng được cập nhật <strong>mỗi giây</strong>
 * qua một Timer daemon — không cần reload lại từ server.</p>
 */
public class MainAuctionController {

    @FXML private Label               lblWelcome;
    @FXML private TableView<AuctionRow> auctionTable;
    @FXML private Button              btnCreateAuction;
    @FXML private Button              btnRefresh;

    // Các trường FXML mới cho thống kê & tìm kiếm
    @FXML private TextField           txtSearch;
    @FXML private Label               lblLiveCount;
    @FXML private Label               lblPaginationInfo;

    // Các cột TableView
    @FXML private TableColumn<AuctionRow, Integer> colStt;
    @FXML private TableColumn<AuctionRow, String>  colAuctionId;
    @FXML private TableColumn<AuctionRow, String>  colItemName;
    @FXML private TableColumn<AuctionRow, Double>  colCurrentPrice;
    @FXML private TableColumn<AuctionRow, Integer> colParticipants;
    @FXML private TableColumn<AuctionRow, String>  colStatus;
    @FXML private TableColumn<AuctionRow, String>  colTimeRemaining;

    private final ObservableList<AuctionRow> auctionData = FXCollections.observableArrayList();
    private AuctionRow selectedAuction;

    /** Timer cập nhật cột "Thời gian còn lại" mỗi giây. */
    private Timer liveCountdownTimer;

    @FXML
    public void initialize() {
        try {
            User currentUser = UserManager.getInstance().getCurrentUser();
            if (currentUser != null) {
                lblWelcome.setText("Xin chào, " + currentUser.getFullName()
                        + " | Vai trò: " + currentUser.getClass().getSimpleName());

                // Ẩn nút Tạo phiên với Bidder và Admin
                boolean isSeller = "Seller".equalsIgnoreCase(currentUser.getClass().getSimpleName());
                btnCreateAuction.setVisible(isSeller);
                btnCreateAuction.setManaged(isSeller);
            }

            // 1. Ánh xạ các thuộc tính vào cột TableColumn
            colStt.setCellValueFactory(cellData -> cellData.getValue().sttProperty().asObject());
            colAuctionId.setCellValueFactory(cellData -> cellData.getValue().auctionIdProperty());
            colItemName.setCellValueFactory(cellData -> cellData.getValue().itemNameProperty());
            colCurrentPrice.setCellValueFactory(cellData -> cellData.getValue().currentPriceProperty().asObject());
            colParticipants.setCellValueFactory(cellData -> cellData.getValue().participantCountProperty().asObject());
            colStatus.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
            colTimeRemaining.setCellValueFactory(cellData -> cellData.getValue().timeRemainingProperty());

            // 2. Cài đặt các custom CellFactory tuyệt đẹp
            setupCustomCellFactories();

            // 3. Tích hợp bộ lọc tìm kiếm Realtime (FilteredList)
            FilteredList<AuctionRow> filteredData = new FilteredList<>(auctionData, p -> true);
            txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
                filteredData.setPredicate(auction -> {
                    if (newValue == null || newValue.isEmpty()) {
                        return true;
                    }
                    String lowerCaseFilter = newValue.toLowerCase();
                    if (auction.getItemName().toLowerCase().contains(lowerCaseFilter)) {
                        return true;
                    } else if (auction.getAuctionId().toLowerCase().contains(lowerCaseFilter)) {
                        return true;
                    }
                    return false;
                });
                updatePaginationLabel(filteredData.size());
            });

            SortedList<AuctionRow> sortedData = new SortedList<>(filteredData);
            sortedData.comparatorProperty().bind(auctionTable.comparatorProperty());
            auctionTable.setItems(sortedData);

            auctionTable.setOnMouseClicked(e ->
                    selectedAuction = auctionTable.getSelectionModel().getSelectedItem());

            loadAuctionDataAsync();

        } catch (Exception e) {
            System.err.println("[MainController] Initialize lỗi: " + e.getMessage());
        }
    }

    private void updatePaginationLabel(int filteredSize) {
        lblPaginationInfo.setText("Hiển thị " + filteredSize + " trên " + auctionData.size() + " phiên đang diễn ra");
    }

    /**
     * Khởi tạo và thiết lập các custom CellFactory cho từng cột của TableView
     */
    private void setupCustomCellFactories() {
        // Cột STT: Định dạng hai số dạng 01, 02...
        colStt.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%02d", item));
                    setStyle("-fx-text-fill: #64748b; -fx-alignment: center; -fx-font-weight: bold;");
                }
            }
        });

        // Cột Phiên: Thêm mã ký tự #AUC-
        colAuctionId.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("#AUC-" + item);
                    setStyle("-fx-text-fill: #94a3b8; -fx-font-weight: bold;");
                }
            }
        });

        // Cột Vật phẩm: Thêm biểu tượng trực quan emoji dựa theo tên sản phẩm
        colItemName.setCellFactory(column -> new TableCell<>() {
            private final HBox container = new HBox(8);
            private final StackPane imgPlaceholder = new StackPane();
            private final Label lblIcon = new Label("📦");
            private final Label lblName = new Label();
            {
                container.setAlignment(Pos.CENTER_LEFT);
                imgPlaceholder.setPrefSize(28, 28);
                imgPlaceholder.setMinSize(28, 28);
                imgPlaceholder.setMaxSize(28, 28);
                imgPlaceholder.setStyle("-fx-background-color: #1e293b; -fx-background-radius: 6; -fx-border-color: #334155; -fx-border-radius: 6;");
                lblIcon.setStyle("-fx-font-size: 14px;");
                imgPlaceholder.getChildren().add(lblIcon);
                lblName.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                container.getChildren().addAll(imgPlaceholder, lblName);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    lblName.setText(item);
                    String lower = item.toLowerCase();
                    if (lower.contains("đồng hồ") || lower.contains("rolex") || lower.contains("watch")) {
                        lblIcon.setText("⌚");
                    } else if (lower.contains("tranh") || lower.contains("art") || lower.contains("painting")) {
                        lblIcon.setText("🖼️");
                    } else if (lower.contains("sách") || lower.contains("book")) {
                        lblIcon.setText("📖");
                    } else if (lower.contains("laptop") || lower.contains("phone") || lower.contains("computer")) {
                        lblIcon.setText("💻");
                    } else {
                        lblIcon.setText("📦");
                    }
                    setGraphic(container);
                }
            }
        });

        // Cột Giá: Định dạng dấu phẩy phân tách phần nghìn
        colCurrentPrice.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%,.0f", item));
                    setStyle("-fx-text-fill: #10b981; -fx-font-weight: 800;");
                }
            }
        });

        // Cột Số người tham gia: Thêm icon nhóm người
        colParticipants.setCellFactory(column -> new TableCell<>() {
            private final HBox container = new HBox(6);
            private final Label lblIcon = new Label("👥");
            private final Label lblCount = new Label();
            {
                container.setAlignment(Pos.CENTER_LEFT);
                lblIcon.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");
                lblCount.setStyle("-fx-text-fill: #cbd5e1; -fx-font-weight: 600;");
                container.getChildren().addAll(lblIcon, lblCount);
            }

            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    lblCount.setText(String.valueOf(item));
                    setGraphic(container);
                }
            }
        });

        // Cột Trạng thái: Dạng badge viên thuốc bo tròn màu sắc
        colStatus.setCellFactory(column -> new TableCell<>() {
            private final Label badge = new Label();
            {
                badge.setPadding(new Insets(4, 10, 4, 10));
                badge.setStyle("-fx-background-radius: 12; -fx-font-weight: bold; -fx-font-size: 11px;");
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    badge.setText("• " + item.toUpperCase());
                    if ("RUNNING".equalsIgnoreCase(item) || "ĐANG DIỄN RA".equalsIgnoreCase(item)) {
                        badge.setStyle("-fx-background-color: rgba(16, 185, 129, 0.15); -fx-text-fill: #34d399; -fx-background-radius: 12; -fx-font-weight: bold; -fx-font-size: 11px;");
                    } else if ("FINISHED".equalsIgnoreCase(item) || "ĐÃ KẾT THÚC".equalsIgnoreCase(item)) {
                        badge.setStyle("-fx-background-color: rgba(239, 68, 68, 0.15); -fx-text-fill: #f87171; -fx-background-radius: 12; -fx-font-weight: bold; -fx-font-size: 11px;");
                    } else {
                        badge.setStyle("-fx-background-color: rgba(59, 130, 246, 0.15); -fx-text-fill: #60a5fa; -fx-background-radius: 12; -fx-font-weight: bold; -fx-font-size: 11px;");
                    }
                    setGraphic(badge);
                }
            }
        });

        // Cột Đồng hồ đếm ngược: Đậm màu cam nổi bật
        colTimeRemaining.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    if ("Đã kết thúc".equals(item)) {
                        setStyle("-fx-text-fill: #ef4444; -fx-font-family: 'Courier New'; -fx-font-weight: bold; -fx-font-size: 14px;");
                    } else {
                        setStyle("-fx-text-fill: #f97316; -fx-font-family: 'Courier New'; -fx-font-weight: bold; -fx-font-size: 14px;");
                    }
                }
            }
        });
    }

    // -------------------------------------------------------------------------
    // Load dữ liệu
    // -------------------------------------------------------------------------

    private void loadAuctionDataAsync() {
        Thread loadThread = new Thread(() -> {
            try {
                loadAuctionData();
            } catch (Exception e) {
                System.err.println("[MainController] Lỗi tải dữ liệu: " + e.getMessage());
                Platform.runLater(() ->
                        showAlert(Alert.AlertType.WARNING, "Lỗi tải dữ liệu",
                                "Không thể tải danh sách phiên: " + e.getMessage()));
            }
        });
        loadThread.setDaemon(true);
        loadThread.start();
    }

    /**
     * Tải danh sách phiên đấu giá từ server bằng <strong>một lần gọi duy nhất</strong> (LIST_DETAIL).
     *
     * <p>Format server trả về:
     * {@code DANH_SACH_CHI_TIET|id:itemName:price:status:endTime|...}</p>
     */
    private void loadAuctionData() {
        String response = NetworkClient.getInstance().sendRequest("LIST_DETAIL");

        if (response == null || !response.startsWith("DANH_SACH_CHI_TIET")) {
            Platform.runLater(() ->
                    showAlert(Alert.AlertType.WARNING, "Không tải được dữ liệu",
                            "Không thể lấy danh sách phiên từ máy chủ."));
            return;
        }

        ObservableList<AuctionRow> rows = FXCollections.observableArrayList();
        String[] entries = response.split("\\|");
        int stt = 1;

        for (int i = 1; i < entries.length; i++) {
            if ("trong".equalsIgnoreCase(entries[i])) break;

            // Format: id:itemName:price:status:endTime
            String[] parts = entries[i].split(":", 5);
            if (parts.length < 5) continue;

            String auctionId     = parts[0];
            String itemName      = parts[1];
            double currentPrice  = parseDouble(parts[2]);
            String status        = parts[3];
            String endTimeStr    = parts[4];          // ISO datetime gốc — dùng cho live timer
            String timeRemaining = calculateTimeRemaining(endTimeStr); // hiển thị lần đầu

            rows.add(new AuctionRow(stt++, auctionId, itemName, currentPrice,
                    0, status, endTimeStr, timeRemaining));
        }

        Platform.runLater(() -> {
            auctionData.setAll(rows);
            lblLiveCount.setText(String.valueOf(rows.size()));
            updatePaginationLabel(rows.size());
            startLiveCountdown();   // Khởi động timer đếm ngược realtime
        });
    }

    // -------------------------------------------------------------------------
    // Live countdown timer — cập nhật cột "Thời gian còn lại" mỗi giây
    // -------------------------------------------------------------------------

    /**
     * Bắt đầu Timer daemon cập nhật cột thời gian còn lại mỗi giây.
     * Huỷ timer cũ (nếu có) trước khi tạo timer mới.
     */
    private void startLiveCountdown() {
        if (liveCountdownTimer != null) {
            liveCountdownTimer.cancel();
        }
        liveCountdownTimer = new Timer("main-auction-countdown", true); // daemon
        liveCountdownTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    boolean allDone = true;
                    for (AuctionRow row : auctionData) {
                        String endTimeStr = row.getEndTimeStr();
                        if (endTimeStr == null || endTimeStr.isBlank()) continue;

                        String display = calculateTimeRemaining(endTimeStr);
                        row.timeRemainingProperty().set(display);

                        if (!"Đã kết thúc".equals(display)) {
                            allDone = false;
                        }
                    }
                    // Nếu tất cả đã kết thúc thì huỷ timer — không cần chạy thêm
                    if (allDone && !auctionData.isEmpty()) {
                        liveCountdownTimer.cancel();
                    }
                });
            }
        }, 1000, 1000);
    }

    // -------------------------------------------------------------------------
    // Event Handlers
    // -------------------------------------------------------------------------

    @FXML
    void handleLogout(ActionEvent event) {
        if (liveCountdownTimer != null) liveCountdownTimer.cancel();
        UserManager.getInstance().setCurrentUser(null);
        SceneUtil.changeScene(event, "Login.fxml", "Đăng nhập");
    }

    @FXML
    void handleCreateAuction(ActionEvent event) {
        if (liveCountdownTimer != null) liveCountdownTimer.cancel();
        SceneUtil.changeScene(event, "CreateAuction.fxml", "Tạo phiên đấu giá mới");
    }

    @FXML
    void handleJoinAuction(ActionEvent event) {
        if (selectedAuction == null) {
            showAlert(Alert.AlertType.INFORMATION, "Chưa chọn phiên",
                    "Vui lòng chọn một phiên đấu giá để tham gia!");
            return;
        }
        if (liveCountdownTimer != null) liveCountdownTimer.cancel();
        AuctionRoomController.setSelectedAuction(
                selectedAuction.getAuctionId(), selectedAuction.getItemName());
        SceneUtil.changeScene(event, "AuctionRoom.fxml",
                "Phòng đấu giá: " + selectedAuction.getItemName());
    }

    @FXML
    void handleRefresh(ActionEvent event) {
        if (liveCountdownTimer != null) liveCountdownTimer.cancel();
        loadAuctionDataAsync();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private double parseDouble(String value) {
        try { return Double.parseDouble(value); }
        catch (NumberFormatException e) { return 0.0; }
    }

    private String calculateTimeRemaining(String endTimeStr) {
        if (endTimeStr == null || endTimeStr.isBlank()) return "00:00:00";
        try {
            LocalDateTime endTime = LocalDateTime.parse(endTimeStr);
            long seconds = Duration.between(LocalDateTime.now(), endTime).getSeconds();
            if (seconds <= 0) return "Đã kết thúc";
            return String.format("%02d:%02d:%02d",
                    seconds / 3600, (seconds % 3600) / 60, seconds % 60);
        } catch (Exception e) {
            return "00:00:00";
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
