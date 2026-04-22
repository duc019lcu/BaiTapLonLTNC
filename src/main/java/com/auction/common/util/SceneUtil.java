package com.auction.onlineauctionsystem.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import java.io.IOException;

public class SceneUtil {
    public static void changeScene(ActionEvent event, String fxmlFile, String title) {
        try {
            // Tải file FXML từ thư mục resources
            Parent root = FXMLLoader.load(SceneUtil.class.getResource("/views/" + fxmlFile));
            // Lấy Stage hiện tại từ nút bấm đã kích hoạt sự kiện
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Lỗi: Không tìm thấy file " + fxmlFile);
            e.printStackTrace();
        }
    }
}
