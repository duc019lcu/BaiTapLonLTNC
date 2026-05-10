package com.auction.client.controller;

import com.auction.client.service.NetworkClient;
import com.auction.common.models.User;
import com.auction.common.models.UserManager;
import com.auction.common.util.SceneUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class MainAuctionController {

    @FXML
    private Label lblWelcome;

    @FXML
    private VBox auctionCardsContainer;

    @FXML
    private Label lblEmptyAuctions;

    @FXML
    private Button btnCreateAuction;

    @FXML
    private Button btnJoinAuction;

    private final List<AuctionViewModel> auctionList = new ArrayList<>();
    private AuctionViewModel selectedAuction;
    private HBox selectedCardNode;

    @FXML
    public void initialize() {
        User currentUser = UserManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            lblWelcome.setText("Xin chào, " + currentUser.getFullName() + " | Vai trò: " + currentUser.getClass().getSimpleName());
            
            // Nếu không phải là Seller thì ẩn nút Tạo phiên đấu giá
            if (!currentUser.getClass().getSimpleName().equalsIgnoreCase("Seller")) {
                btnCreateAuction.setVisible(false);
                btnCreateAuction.setManaged(false);
            }
        }

        loadAuctionData();
    }

    private void loadAuctionData() {
        auctionList.clear();
        String response = NetworkClient.getInstance().sendRequest("LIST");
        if (response == null || !response.startsWith("DANH_SACH")) {
            showAlert(Alert.AlertType.WARNING, "Không tải được dữ liệu", "Không thể lấy danh sách phiên từ máy chủ.");
            return;
        }
        String[] entries = response.split("\\|");
        for (int i = 1; i < entries.length; i++) {
            if ("trong".equalsIgnoreCase(entries[i])) {
                renderAuctionCards();
                return;
            }
            String[] parts = entries[i].split(":");
            if (parts.length != 3) {
                continue;
            }
            auctionList.add(new AuctionViewModel(parts[0], "Phiên " + parts[0], formatPrice(parts[1]), parts[2]));
        }
        renderAuctionCards();
    }

    private String formatPrice(String rawPrice) {
        try {
            double value = Double.parseDouble(rawPrice);
            return String.format("%,.0f", value);
        } catch (NumberFormatException ex) {
            return rawPrice;
        }
    }

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
        AuctionViewModel selected = selectedAuction;
        if (selected != null) {
            AuctionRoomController.setSelectedAuction(selected.getId(), selected.getItem());
            SceneUtil.changeScene(event, "AuctionRoom.fxml", "Phòng đấu giá: " + selected.getItem());
        } else {
            showAlert(Alert.AlertType.INFORMATION, "Chưa chọn phiên", "Vui lòng chọn một phiên đấu giá để tham gia!");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void renderAuctionCards() {
        auctionCardsContainer.getChildren().clear();
        if (auctionList.isEmpty()) {
            lblEmptyAuctions.setVisible(true);
            lblEmptyAuctions.setManaged(true);
            auctionCardsContainer.getChildren().add(lblEmptyAuctions);
            return;
        }
        lblEmptyAuctions.setVisible(false);
        lblEmptyAuctions.setManaged(false);

        for (AuctionViewModel auction : auctionList) {
            HBox card = createAuctionCard(auction);
            auctionCardsContainer.getChildren().add(card);
        }
    }

    private HBox createAuctionCard(AuctionViewModel auction) {
        Label idTag = new Label("#" + auction.getId());
        idTag.getStyleClass().add("card-id");

        Label name = new Label(auction.getItem());
        name.getStyleClass().add("card-title");

        Label price = new Label("Giá cao nhất: " + auction.getPrice());
        price.getStyleClass().add("card-sub");

        VBox left = new VBox(4, idTag, name, price);
        left.setAlignment(Pos.CENTER_LEFT);

        Label statusBadge = new Label(auction.getStatus());
        statusBadge.getStyleClass().add("status-badge");
        String status = auction.getStatus().toUpperCase();
        if ("RUNNING".equals(status) || "EXTENDED".equals(status)) {
            statusBadge.getStyleClass().add("status-running");
        } else if ("OPEN".equals(status)) {
            statusBadge.getStyleClass().add("status-open");
        } else {
            statusBadge.getStyleClass().add("status-finished");
        }

        Button joinBtn = new Button("Tham gia");
        joinBtn.getStyleClass().add("btn-primary");
        joinBtn.setOnAction(e -> {
            selectedAuction = auction;
            updateSelectedCardStyle((HBox) ((Node) e.getSource()).getParent().getParent());
        });

        Button detailBtn = new Button("Chi tiết");
        detailBtn.getStyleClass().add("btn-secondary");
        detailBtn.setOnAction(e -> {
            selectedAuction = auction;
            updateSelectedCardStyle((HBox) ((Node) e.getSource()).getParent().getParent());
            showAlert(Alert.AlertType.INFORMATION, "Thông tin phiên",
                    "Mã phiên: " + auction.getId()
                            + "\nTên phiên: " + auction.getItem()
                            + "\nGiá hiện tại: " + auction.getPrice()
                            + "\nTrạng thái: " + auction.getStatus());
        });

        HBox actions = new HBox(8, detailBtn, joinBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);

        VBox right = new VBox(8, statusBadge, actions);
        right.setAlignment(Pos.CENTER_RIGHT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox card = new HBox(12, left, spacer, right);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(12));
        card.getStyleClass().add("auction-card");
        card.setOnMouseClicked(e -> {
            selectedAuction = auction;
            updateSelectedCardStyle(card);
        });
        return card;
    }

    private void updateSelectedCardStyle(HBox newSelectedCard) {
        if (selectedCardNode != null) {
            selectedCardNode.getStyleClass().remove("auction-card-selected");
        }
        selectedCardNode = newSelectedCard;
        if (!selectedCardNode.getStyleClass().contains("auction-card-selected")) {
            selectedCardNode.getStyleClass().add("auction-card-selected");
        }
    }

    // Lớp ViewModel nội bộ dùng để hiển thị dữ liệu phiên đấu giá
    public static class AuctionViewModel {
        private final String id;
        private final String item;
        private final String price;
        private final String status;

        public AuctionViewModel(String id, String item, String price, String status) {
            this.id = id;
            this.item = item;
            this.price = price;
            this.status = status;
        }
        
        public String getId() { return id; }
        public String getItem() { return item; }
        public String getPrice() { return price; }
        public String getStatus() { return status; }
    }
}
