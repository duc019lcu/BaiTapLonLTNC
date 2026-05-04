package com.bai_tap_lon;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Trỏ đường dẫn tới file FXML của bạn
        URL fxmlLocation = getClass().getResource("/views/SellerItemManager.fxml");

        if (fxmlLocation == null) {
            System.out.println("LỖI: Không tìm thấy file SellerItemManager.fxml! Hãy kiểm tra lại thư mục resources.");
            return;
        }

        Parent root = FXMLLoader.load(fxmlLocation);

        // Tạo cửa sổ ứng dụng
        Scene scene = new Scene(root, 1000, 600);
        primaryStage.setTitle("Hệ thống Đấu Giá - Quản lý Hàng Hóa");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args); // Lệnh này sẽ kích hoạt JavaFX khởi động
    }
}