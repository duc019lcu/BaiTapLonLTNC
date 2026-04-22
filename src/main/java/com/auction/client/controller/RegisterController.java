package com.auction.onlineauctionsystem.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import com.auction.onlineauctionsystem.model.User;
import com.auction.onlineauctionsystem.model.UserManager;
import com.auction.onlineauctionsystem.model.Seller;
import com.auction.onlineauctionsystem.util.SceneUtil;
import com.auction.onlineauctionsystem.model.Bidder;

/**
 * Tác dụng: Lớp điều khiển (Controller) cho giao diện Đăng ký tài khoản (Register).
 * Nhiệm vụ chính: Xử lý logic thu thập thông tin người dùng, khởi tạo đối tượng
 * người dùng theo vai trò (Buyer/Seller) và thực hiện đăng ký thông qua UserManager.
 */
public class RegisterController {

    // Các trường nhập liệu từ file FXML
    @FXML private TextField txtUser;      // Nhập tên đăng nhập
    @FXML private TextField txtEmail;     // Nhập email liên lạc
    @FXML private TextField txtAddress;   // Nhập địa chỉ (đặc biệt cho Bidder)
    @FXML private PasswordField txtPass;   // Nhập mật khẩu
    @FXML private ComboBox<String> cbRole; // Lựa chọn vai trò: "Bidder" hoặc "Seller"

    /**
     * Phương thức khởi tạo tự động của JavaFX.
     * Tác dụng: Cài đặt các giá trị ban đầu cho các thành phần giao diện ngay khi màn hình được load.
     */
    @FXML
    public void initialize() {
        // Nạp danh sách lựa chọn vào ComboBox để người dùng chọn vai trò
        cbRole.getItems().addAll("Người mua (Bidder)", "Người bán (Seller)");
        // Đặt giá trị mặc định để tránh lỗi khi người dùng không chọn
        cbRole.setValue("Người mua (Bidder)");
    }

    /**
     * Xử lý sự kiện khi người dùng nhấn nút "Đăng ký".
     * Tác dụng: Chuyển đổi dữ liệu từ UI thành đối tượng Model và gọi nghiệp vụ đăng ký.
     */
    @FXML
    void handleRegister(ActionEvent event) {
        String username = txtUser.getText();
        String role = cbRole.getValue();

        // 1. Sinh ID tự động thông qua Manager (đảm bảo tính duy nhất)
        long id = UserManager.getInstance().generateId();

        // 2. Sử dụng tính Đa hình (Polymorphism) để khởi tạo đối tượng cụ thể
        User newUser;
        if (role.contains("Seller")) {
            // Nếu chọn Seller, khởi tạo đối tượng Seller
            newUser = new Seller(id, username, txtPass.getText(), txtEmail.getText());
        } else {
            // Nếu chọn Bidder, khởi tạo đối tượng Bidder (có thêm trường Address)
            newUser = new Bidder(id, username, txtPass.getText(), txtEmail.getText(), txtAddress.getText());
        }

        // 3. Gọi hàm nghiệp vụ đăng ký tài khoản từ UserManager
        if (UserManager.getInstance().register(newUser)) {
            // Hiển thị thông báo thành công nếu tên đăng nhập chưa tồn tại
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Đăng ký thành công!");
            alert.showAndWait();

            // 4. Chuyển hướng người dùng quay lại trang Đăng nhập sau khi đăng ký xong
            SceneUtil.changeScene(event, "Login.fxml", "Đăng nhập");
        } else {
            // Hiển thị thông báo lỗi nếu tên đăng nhập đã có người sử dụng
            Alert alert = new Alert(Alert.AlertType.ERROR, "Tên đăng nhập đã tồn tại!");
            alert.show();
        }
    }
}