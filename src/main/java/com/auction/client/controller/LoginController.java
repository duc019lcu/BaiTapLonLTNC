package com.auction.client.controller;

import com.auction.client.service.NetworkClient;
import com.auction.common.models.Admin;
import com.auction.common.models.Bidder;
import com.auction.common.models.Seller;
import com.auction.common.models.User;
import com.auction.common.models.UserManager;
import com.auction.common.util.SceneUtil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblMessage;

    @FXML
    void handleLogin(ActionEvent event) {
        String user = txtUsername.getText().trim();
        String pass = txtPassword.getText().trim();

        if (user.isEmpty() || pass.isEmpty()) {
            lblMessage.setText("Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        lblMessage.setText("Đang kết nối tới máy chủ...");
        lblMessage.setStyle("-fx-text-fill: #60a5fa;");

        new Thread(() -> {
            NetworkClient client = NetworkClient.getInstance();
            if (!client.connect("localhost", 9999)) {
                Platform.runLater(() -> {
                    lblMessage.setText("Không thể kết nối đến Máy chủ!");
                    lblMessage.setStyle("-fx-text-fill: #f87171;");
                });
                return;
            }

            String response = client.sendRequest("LOGIN|" + user + "|" + pass);

            Platform.runLater(() -> {
                if (response != null && response.startsWith("LOGIN_SUCCESS")) {
                    String[] parts = response.split("\\|");
                    String role     = parts[1];
                    String id       = parts[2];
                    String fullName = parts[3];
                    String email    = parts[4];

                    User loggedInUser = null;
                    if ("ADMIN".equalsIgnoreCase(role)) {
                        loggedInUser = new Admin(id, user, pass, fullName, email);
                    } else if ("SELLER".equalsIgnoreCase(role)) {
                        loggedInUser = new Seller(id, user, pass, fullName, email);
                    } else if ("BIDDER".equalsIgnoreCase(role)) {
                        // FIX: Nhận balance từ server (parts[5]), mặc định 0 nếu không có
                        double balance = 0.0;
                        if (parts.length > 5) {
                            try { balance = Double.parseDouble(parts[5]); }
                            catch (NumberFormatException ignored) {}
                        }
                        loggedInUser = new Bidder(id, user, pass, fullName, email, balance);
                    }

                    UserManager.getInstance().setCurrentUser(loggedInUser);

                    if ("ADMIN".equalsIgnoreCase(role)) {
                        SceneUtil.changeScene(event, "AdminDashboard.fxml", "Quản trị viên");
                    } else {
                        SceneUtil.changeScene(event, "MainAuction.fxml", "Sàn Đấu Giá");
                    }
                } else {
                    String[] parts = response != null ? response.split("\\|") : new String[0];
                    lblMessage.setText(parts.length > 1 ? parts[1] : "Đăng nhập thất bại!");
                    lblMessage.setStyle("-fx-text-fill: #f87171;");
                }
            });
        }).start();
    }

    @FXML
    void goToRegister(ActionEvent event) {
        SceneUtil.changeScene(event, "Register.fxml", "Đăng ký tài khoản");
    }
}