package com.auction.client.controller;

import com.auction.common.models.Bidder;
import com.auction.common.models.Seller;
import com.auction.common.models.User;
import com.auction.common.models.UserManager;
import com.auction.common.util.SceneUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController {

    @FXML
    private TextField txtUser;

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtAddress;

    @FXML
    private PasswordField txtPass;

    @FXML
    private ComboBox<String> cbRole;

    @FXML
    public void initialize() {
        cbRole.getItems().addAll("Nguoi mua (Bidder)", "Nguoi ban (Seller)");
        cbRole.setValue("Nguoi mua (Bidder)");
    }

    @FXML
    void handleRegister(ActionEvent event) {
        String username = txtUser.getText();
        String role = cbRole.getValue();
        long id = UserManager.getInstance().generateId();

        User newUser;
        if (role.contains("Seller")) {
            newUser = new Seller(String.valueOf(id), username, txtPass.getText(), username, txtEmail.getText());
        } else {
            newUser = new Bidder(String.valueOf(id), username, txtPass.getText(), username, txtEmail.getText(), 0.0);
        }

        if (UserManager.getInstance().register(newUser)) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Dang ky thanh cong!");
            alert.showAndWait();
            SceneUtil.changeScene(event, "Login.fxml", "Dang nhap");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.ERROR, "Ten dang nhap da ton tai!");
        alert.show();
    }

    @FXML
    void handleLoginRedirect(ActionEvent event) {
        SceneUtil.changeScene(event, "Login.fxml", "Dang nhap");
    }
}
