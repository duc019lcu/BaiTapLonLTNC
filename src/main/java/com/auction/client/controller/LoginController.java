package com.auction.onlineauctionsystem.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import com.auction.onlineauctionsystem.model.User;
import com.auction.onlineauctionsystem.model.UserManager;
import com.auction.onlineauctionsystem.model.Admin;
import com.auction.onlineauctionsystem.util.SceneUtil;

/**
 * Tác dụng: Lớp điều khiển (Controller) cho giao diện Đăng nhập (Login).
 * Nhiệm vụ chính: Tiếp nhận thông tin từ người dùng, xác thực tài khoản qua UserManager
 * và điều phối chuyển cảnh dựa trên vai trò (Role) của người dùng.
 */
public class LoginController {

    // Các thành phần giao diện được liên kết từ file FXML
    @FXML private TextField txtUsername;    // Ô nhập tên đăng nhập
    @FXML private PasswordField txtPassword; // Ô nhập mật khẩu (ẩn ký tự)
    @FXML private Label lblMessage;         // Nhãn hiển thị thông báo lỗi hoặc trạng thái cho người dùng

    /**
     * Xử lý sự kiện khi người dùng nhấn nút "Đăng nhập".
     * @param event Sự kiện hành động từ UI
     */
    @FXML
    void handleLogin(ActionEvent event) {
        String user = txtUsername.getText();
        String pass = txtPassword.getText();

        // 1. Kiểm tra tính hợp lệ của dữ liệu đầu vào (Validation)
        if (user.isEmpty() || pass.isEmpty()) {
            lblMessage.setText("Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        // 2. Gọi lớp nghiệp vụ (UserManager) để kiểm tra thông tin đăng nhập
        User loggedInUser = UserManager.getInstance().login(user, pass);

        if (loggedInUser != null) {
            // 3. Lưu phiên đăng nhập (Session) của người dùng hiện tại vào hệ thống
            UserManager.getInstance().setCurrentUser(loggedInUser);

            // 4. Phân quyền và điều hướng (Role-based Navigation)
            // Nếu là Admin thì chuyển đến bảng điều khiển quản trị
            if (loggedInUser instanceof Admin) {
                SceneUtil.changeScene(event, "AdminDashboard.fxml", "Quản trị viên");
            }
            // Nếu là người dùng thường thì chuyển đến sàn đấu giá
            else {
                SceneUtil.changeScene(event, "MainAuction.fxml", "Sàn Đấu Giá");
            }
        } else {
            // 5. Thông báo nếu xác thực thất bại
            lblMessage.setText("Sai tài khoản hoặc mật khẩu!");
        }
    }

    /**
     * Xử lý sự kiện khi người dùng nhấn link/nút "Đăng ký".
     * Tác dụng: Chuyển hướng người dùng sang màn hình tạo tài khoản mới.
     */
    @FXML
    void goToRegister(ActionEvent event) {
        SceneUtil.changeScene(event, "Register.fxml", "Đăng ký tài khoản");
    }
}