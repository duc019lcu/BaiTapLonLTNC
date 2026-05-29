package com.auction.client.controller;

import com.auction.client.service.NetworkClient;
import com.auction.common.models.Bidder;
import com.auction.common.models.User;
import com.auction.common.models.UserManager;
import com.auction.common.util.SceneUtil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class ProfileController {

    @FXML private Label     lblAvatar;
    @FXML private Label     lblUsername;
    @FXML private Label     lblRole;
    @FXML private Label     lblBalance;
    @FXML private Label     lblFrozen;
    @FXML private Label     lblEmail;
    @FXML private Label     lblDepositMsg;
    @FXML private TextField txtDeposit;

    private User currentUser;

    @FXML
    public void initialize() {
        currentUser = UserManager.getInstance().getCurrentUser();
        if (currentUser == null) return;

        // Avatar: lấy chữ cái đầu username
        String name = currentUser.getUsername();
        lblAvatar.setText(name.substring(0, 1).toUpperCase());
        lblUsername.setText(name);
        lblRole.setText("Vai trò: " + currentUser.getRole());
        lblEmail.setText(currentUser.getEmail());

        // Load ví tiền nếu là Bidder
        if (currentUser instanceof Bidder) {
            loadWalletAsync();
        } else {
            lblBalance.setText("N/A");
            lblFrozen.setText("N/A");
        }
    }

    private void loadWalletAsync() {
        new Thread(() -> {
            String res = NetworkClient.getInstance()
                    .sendRequest("GET_PROFILE|" + currentUser.getId());
            Platform.runLater(() -> {
                if (res != null && res.startsWith("PROFILE_SUCCESS")) {
                    String[] parts = res.split("\\|");
                    double balance = parseDouble(parts[1]);
                    double frozen  = parseDouble(parts[2]);
                    lblBalance.setText(String.format("%,.0f ₫", balance));
                    lblFrozen.setText(String.format("%,.0f ₫", frozen));
                } else {
                    lblBalance.setText("Lỗi tải");
                    lblFrozen.setText("Lỗi tải");
                }
            });
        }).start();
    }

    @FXML
    void handleDeposit(ActionEvent event) {
        String raw = txtDeposit.getText().trim();
        double amount;
        try {
            amount = Double.parseDouble(raw.replace(",", ""));
        } catch (NumberFormatException e) {
            showMsg("Vui lòng nhập số tiền hợp lệ.", false);
            return;
        }
        if (amount <= 0) {
            showMsg("Số tiền phải lớn hơn 0.", false);
            return;
        }

        new Thread(() -> {
            String res = NetworkClient.getInstance()
                    .sendRequest("DEPOSIT|" + currentUser.getId() + "|" + amount);
            Platform.runLater(() -> {
                if (res != null && res.startsWith("DEPOSIT_SUCCESS")) {
                    double newBalance = parseDouble(res.split("\\|")[1]);
                    lblBalance.setText(String.format("%,.0f ₫", newBalance));
                    txtDeposit.clear();
                    showMsg("✓ Nạp tiền thành công!", true);
                } else {
                    showMsg("Nạp tiền thất bại: " + res, false);
                }
            });
        }).start();
    }

    @FXML
    void handleBack(ActionEvent event) {
        User user = UserManager.getInstance().getCurrentUser();
        if (user == null) return;
        String role = user.getRole().toUpperCase();
        switch (role) {
            case "ADMIN"  -> SceneUtil.changeScene(event, "AdminDashboard.fxml", "Admin");
            case "SELLER" -> SceneUtil.changeScene(event, "MainAuction.fxml", "Sàn đấu giá");
            default       -> SceneUtil.changeScene(event, "MainAuction.fxml", "Sàn đấu giá");
        }
    }

    private void showMsg(String msg, boolean success) {
        lblDepositMsg.setText(msg);
        lblDepositMsg.setStyle(
            "-fx-font-size: 13px; -fx-text-fill: "
            + (success ? "#10b981" : "#ef4444") + ";"
        );
    }

    private double parseDouble(String s) {
        try { return Double.parseDouble(s); }
        catch (Exception e) { return 0; }
    }
}