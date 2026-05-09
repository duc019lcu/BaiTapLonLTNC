package com.auction.client.controller;

import com.auction.common.util.SceneUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class CreateAuctionController {

    @FXML
    private TextField txtItemName;

    @FXML
    private ComboBox<String> cbCategory;

    @FXML
    private TextArea txtDescription;

    @FXML
    private TextField txtStartPrice;

    @FXML
    private TextField txtDuration; // Nhập số phút

    @FXML
    public void initialize() {
        cbCategory.getItems().addAll("Điện tử (Electronics)", "Nghệ thuật (Art)", "Phương tiện (Vehicle)", "Thời trang (Fashion)", "Khác");
        cbCategory.getSelectionModel().selectFirst();
    }

    @FXML
    void handleCreate(ActionEvent event) {
        String name = txtItemName.getText();
        String priceStr = txtStartPrice.getText();
        String durationStr = txtDuration.getText();

        if (name.isEmpty() || priceStr.isEmpty() || durationStr.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập đầy đủ Tên, Giá khởi điểm và Thời gian!");
            return;
        }

        try {
            double price = Double.parseDouble(priceStr);
            int duration = Integer.parseInt(durationStr);
            
            // TODO: Gửi request Socket lên Server để tạo phiên đấu giá
            // NetworkClient.getInstance().sendRequest("CREATE_AUCTION|" + name + "|" + price + "...");

            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã tạo phiên đấu giá thành công!");
            SceneUtil.changeScene(event, "MainAuction.fxml", "Sàn Đấu Giá");

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi định dạng", "Giá và Thời gian phải là số hợp lệ!");
        }
    }

    @FXML
    void handleCancel(ActionEvent event) {
        SceneUtil.changeScene(event, "MainAuction.fxml", "Sàn Đấu Giá");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
