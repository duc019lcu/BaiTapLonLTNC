package com.auction.client.controller;

import com.auction.common.models.Admin;
import com.auction.common.models.User;
import com.auction.common.models.UserManager;
import com.auction.common.util.SceneUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Label lblMessage;

    @FXML
    void handleLogin(ActionEvent event) {
        String user = txtUsername.getText();
        String pass = txtPassword.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            lblMessage.setText("Vui long nhap day du thong tin!");
            return;
        }

        User loggedInUser = UserManager.getInstance().login(user, pass);
        if (loggedInUser == null) {
            lblMessage.setText("Sai tai khoan hoac mat khau!");
            return;
        }

        UserManager.getInstance().setCurrentUser(loggedInUser);
        if (loggedInUser instanceof Admin) {
            SceneUtil.changeScene(event, "AdminDashboard.fxml", "Quan tri vien");
            return;
        }

        SceneUtil.changeScene(event, "MainAuction.fxml", "San Dau Gia");
    }

    @FXML
    void goToRegister(ActionEvent event) {
        SceneUtil.changeScene(event, "Register.fxml", "Dang ky tai khoan");
    }
}
