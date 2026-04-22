package com.auction.onlineauctionsystem.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import java.io.IOException;

/**
 * Tác dụng: Lớp tiện ích quản lý việc chuyển đổi giao diện (Scene) trong ứng dụng.
 * Giúp tập trung logic chuyển cảnh vào một nơi duy nhất, dễ bảo trì và tái sử dụng.
 */
public class SceneUtil {

    /**
     * Chuyển đổi màn hình hiện tại sang một màn hình mới dựa trên file FXML.
     * * @param event    Sự kiện từ UI (thường là Click chuột vào Button) để xác định cửa sổ hiện tại.
     * @param fxmlFile Tên file giao diện (.fxml) nằm trong thư mục /views/.
     * @param title    Tiêu đề mới cho cửa sổ (Stage).
     */
    public static void changeScene(ActionEvent event, String fxmlFile, String title) {
        try {
            // 1. Tải sơ đồ giao diện từ file FXML dựa trên đường dẫn tương đối trong resources
            Parent root = FXMLLoader.load(SceneUtil.class.getResource("/views/" + fxmlFile));

            // 2. Truy xuất Stage (cửa sổ) hiện tại từ đối tượng gây ra sự kiện (ví dụ: Button)
            // Node -> Scene -> Window (ép kiểu về Stage)
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // 3. Cập nhật thông tin tiêu đề và thiết lập Scene mới cho Stage
            stage.setTitle(title);
            stage.setScene(new Scene(root));

            // 4. Hiển thị cửa sổ đã cập nhật
            stage.show();

        } catch (IOException e) {
            // Xử lý lỗi trong trường hợp đường dẫn sai hoặc file FXML bị hỏng
            System.err.println("Lỗi nghiêm trọng: Không thể tải được file giao diện: " + fxmlFile);
            System.err.println("Hãy kiểm tra lại thư mục src/main/resources/views/");
            e.printStackTrace();
        } catch (NullPointerException e) {
            // Bắt lỗi nếu getResource trả về null (thường do sai đường dẫn)
            System.err.println("Lỗi: Đường dẫn file FXML không tồn tại (null).");
        }
    }
}