package com.auction.onlineauctionsystem.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.*;
import util.SceneUtil;

public class RegisterController {
    @FXML private TextField txtUser, txtEmail, txtAddress;
    @FXML private PasswordField txtPass;
    @FXML private ComboBox<String> cbRole; // "Bidder" hoặc "Seller"

    @FXML
    public void initialize() {
        // Thêm lựa chọn vào ComboBox khi màn hình hiện lên
        cbRole.getItems().addAll("Người mua (Bidder)", "Người bán (Seller)");
        cbRole.setValue("Người mua (Bidder)");
    }

    @FXML
    void handleRegister(ActionEvent event) {
        String username = txtUser.getText();
        String role = cbRole.getValue();
        long id = UserManager.getInstance().generateId();

        User newUser;
        if (role.contains("Seller")) {
            newUser = new Seller(id, username, txtPass.getText(), txtEmail.getText());
        } else {
            newUser = new Bidder(id, username, txtPass.getText(), txtEmail.getText(), txtAddress.getText());
        }

        if (UserManager.getInstance().register(newUser)) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Đăng ký thành công!");
            alert.showAndWait();
            SceneUtil.changeScene(event, "Login.fxml", "Đăng nhập");
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Tên đăng nhập đã tồn tại!");
            alert.show();
        }
    }
}