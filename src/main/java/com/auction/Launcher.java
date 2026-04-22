package com.auction.onlineauctionsystem;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
public class Launcher extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Đây là dòng quan trọng nhất: Nó nạp giao diện bạn đã vẽ vào App
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/auction/onlineauctionsystem/fxml/Login.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Hệ Thống Đấu Giá");
        stage.setScene(scene);
        stage.show(); // Lệnh này mới làm cái App hiện lên màn hình
    }

    public static void main(String[] args) {
        launch();
    }
}
