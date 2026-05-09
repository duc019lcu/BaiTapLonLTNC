package com.auction.client.controller;

import com.auction.common.models.User;
import com.auction.common.models.UserManager;
import com.auction.common.util.SceneUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class MainAuctionController {

    @FXML
    private Label lblWelcome;

    @FXML
    private TableView<AuctionViewModel> tableAuctions;

    @FXML
    private TableColumn<AuctionViewModel, String> colId;

    @FXML
    private TableColumn<AuctionViewModel, String> colItem;

    @FXML
    private TableColumn<AuctionViewModel, String> colPrice;

    @FXML
    private TableColumn<AuctionViewModel, String> colStatus;

    @FXML
    private Button btnCreateAuction;

    @FXML
    private Button btnJoinAuction;

    private ObservableList<AuctionViewModel> auctionList = FXCollections.observableArrayList();

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

        colId.setCellValueFactory(data -> data.getValue().idProperty());
        colItem.setCellValueFactory(data -> data.getValue().itemProperty());
        colPrice.setCellValueFactory(data -> data.getValue().priceProperty());
        colStatus.setCellValueFactory(data -> data.getValue().statusProperty());

        tableAuctions.setItems(auctionList);
        
        loadMockData();
    }

    private void loadMockData() {
        // Tạm thời hiển thị dữ liệu giả để bạn xem trước giao diện. 
        // Sau này sẽ gọi NetworkClient để lấy dữ liệu thật từ Server.
        auctionList.add(new AuctionViewModel("A01", "Macbook Pro M3", "30,000,000", "RUNNING"));
        auctionList.add(new AuctionViewModel("A02", "iPhone 15 Pro Max", "25,000,000", "OPEN"));
        auctionList.add(new AuctionViewModel("A03", "Tranh phong cảnh cổ điển", "5,000,000", "FINISHED"));
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
        AuctionViewModel selected = tableAuctions.getSelectionModel().getSelectedItem();
        if (selected != null) {
            SceneUtil.changeScene(event, "AuctionRoom.fxml", "Phòng đấu giá: " + selected.getItem());
        } else {
            System.out.println("Vui lòng chọn một phiên đấu giá để tham gia!");
        }
    }

    // Lớp ViewModel nội bộ dùng để hiển thị dữ liệu lên TableView
    public static class AuctionViewModel {
        private final SimpleStringProperty id;
        private final SimpleStringProperty item;
        private final SimpleStringProperty price;
        private final SimpleStringProperty status;

        public AuctionViewModel(String id, String item, String price, String status) {
            this.id = new SimpleStringProperty(id);
            this.item = new SimpleStringProperty(item);
            this.price = new SimpleStringProperty(price);
            this.status = new SimpleStringProperty(status);
        }

        public SimpleStringProperty idProperty() { return id; }
        public SimpleStringProperty itemProperty() { return item; }
        public SimpleStringProperty priceProperty() { return price; }
        public SimpleStringProperty statusProperty() { return status; }
        
        public String getId() { return id.get(); }
        public String getItem() { return item.get(); }
    }
}
