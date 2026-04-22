package com.auction.onlineauctionsystem.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import com.auction.onlineauctionsystem.model.User;
import com.auction.onlineauctionsystem.model.UserManager;
import com.auction.onlineauctionsystem.model.Admin;
import com.auction.onlineauctionsystem.util.SceneUtil;

public class LoginController {
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblMessage;

    @FXML
    void handleLogin(ActionEvent event) {
        String user = txtUsername.getText();
        String pass = txtPassword.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            lblMessage.setText("Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        // Gọi logic kiểm tra từ UserManager của tuần 2
        User loggedInUser = UserManager.getInstance().login(user, pass);

        if (loggedInUser != null) {
            // Lưu session người dùng hiện tại
            UserManager.getInstance().setCurrentUser(loggedInUser);

            // Chuyển màn hình dựa trên vai trò
            if (loggedInUser instanceof Admin) {
                SceneUtil.changeScene(event, "AdminDashboard.fxml", "Quản trị viên");
            } else {
                SceneUtil.changeScene(event, "MainAuction.fxml", "Sàn Đấu Giá");
            }
        } else {
            lblMessage.setText("Sai tài khoản hoặc mật khẩu!");
        }
    }

    @FXML
    void goToRegister(ActionEvent event) {
        SceneUtil.changeScene(event, "Register.fxml", "Đăng ký tài khoản");
    }
}