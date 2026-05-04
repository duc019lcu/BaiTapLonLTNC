package com.bai_tap_lon.controller;

//Nếu file FXML là bản vẽ ngôi nhà và các công tắc điện, thì file Controller chính là hệ thống dây điện và bộ não nằm ẩn bên trong bức tường. Nút bấm trên tường (FXML) chỉ vô tri vô giác nếu không có dây điện (Controller) nối vào nó để ra lệnh "Sáng đèn!".
import com.bai_tap_lon.dao.ItemDAO;
import com.bai_tap_lon.factory.ItemFactory;
import com.bai_tap_lon.model.Item;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class SellerController implements Initializable {
    //Có cái fx:id="txtName" bên file FXML, khi bạn viết @FXML private TextField txtName; bên Java, chương trình sẽ tự động lấy cái ô nhập liệu trên màn hình gắn chặt vào biến txtName này.Từ lúc này, bên Java chỉ cần gọi txtName.getText() là lôi được chữ mà người dùng vừa gõ trên màn hình ra.
    // Các trường nhập liệu chung
    @FXML private TextField txtId, txtName, txtInitPrice;
    @FXML private TextArea txtDescription;
    @FXML private ComboBox<String> cbCategory;

    // Các vùng nhập liệu đặc thù
    @FXML private VBox vboxElectronics, vboxArt, vboxVehicle;
    @FXML private TextField txtElec1, txtElec2, txtElec3, txtElec4;
    @FXML private TextField txtArt1, txtArt2, txtArt3;
    @FXML private TextField txtVeh1, txtVeh2, txtVeh3;

    // Bảng hiển thị
    @FXML private TableView<Item> tableItems;
    @FXML private TableColumn<Item, String> colId, colName, colCategory, colDesc;
    @FXML private TableColumn<Item, Double> colPrice;

    //Ý nghĩa: Observable có nghĩa là "có thể quan sát được". Đây là một cái danh sách biết tự động báo cáo. ngay khoảnh khắc bạn nhét thêm 1 món hàng vào nó (itemList.add(newItem)), nó sẽ lập tức "hét" lên: "Ê giao diện, tao có dữ liệu mới nè, vẽ lại bảng ngay!".
    private ObservableList<Item> itemList = FXCollections.observableArrayList();

    //Khi bạn mở app lên, trước khi cái cửa sổ kịp hiện ra cho bạn nhìn thấy, Java sẽ chạy lén hàm này một lần duy nhất.
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 1. Thiết lập các cột cho TableView
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("initPrice"));
        colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));

        // 2. Load dữ liệu từ file CSV bằng DAO
        itemList.addAll(ItemDAO.loadItems());
        tableItems.setItems(itemList);

        // 3. Nạp danh sách loại hàng
        cbCategory.getItems().addAll("ELECTRONICS", "ART", "VEHICLE", "FASHION", "FURNITURE");
    }

    //Đây là những hàm sẽ được gắn với cái "cò súng" onAction="#tên_hàm" bên FXML. Người dùng bấm nút nào, thợ đó chạy ra làm việc.
    @FXML
    public void handleCategoryChange() {
        String selected = cbCategory.getValue();
        // Ẩn tất cả trước
        vboxElectronics.setVisible(false); vboxElectronics.setManaged(false);
        vboxArt.setVisible(false); vboxArt.setManaged(false);
        vboxVehicle.setVisible(false); vboxVehicle.setManaged(false);

        if (selected == null) return;

        // Hiện vùng tương ứng
        switch (selected) {
            case "ELECTRONICS": vboxElectronics.setVisible(true); vboxElectronics.setManaged(true); break;
            case "ART": vboxArt.setVisible(true); vboxArt.setManaged(true); break;
            case "VEHICLE": vboxVehicle.setVisible(true); vboxVehicle.setManaged(true); break;
        }
    }

    //Chạy khi bấm nút THÊM MỚI. Quá trình làm việc của nó chuẩn y như dây chuyền nhà máy:
    //
    //Gom nguyên liệu: Đi thu thập chữ từ các ô nhập liệu (txtId.getText(), txtName.getText()). Ép giá tiền từ chữ thành số (Double.parseDouble).
    //
    //Quăng vào máy tạo nặn: Đưa đống nguyên liệu đó cho máy Factory (ItemFactory.createItem(...)). Nhờ máy này nặn ra đúng hình thù một cái Máy tính (Electronics) hay Bức tranh (Art).
    @FXML
    public void handleAdd() {
        try {
            String type = cbCategory.getValue();

            // 1. Kiểm tra lỗi chưa chọn danh mục
            if (type == null || type.trim().isEmpty()) {
                showAlert("Lỗi nhập liệu", "Vui lòng chọn loại hàng hóa từ danh sách.");
                return; // Dừng lại, không chạy tiếp xuống dưới
            }

            // Lấy dữ liệu và loại bỏ khoảng trắng thừa ở hai đầu
            String id = txtId.getText().trim();
            String name = txtName.getText().trim();
            String desc = txtDescription.getText().trim();
            String priceText = txtInitPrice.getText().trim();

            // 2. Kiểm tra lỗi bỏ trống các trường bắt buộc
            if (id.isEmpty() || name.isEmpty() || priceText.isEmpty()) {
                showAlert("Lỗi nhập liệu", "Vui lòng nhập đầy đủ ID, Tên sản phẩm và Giá khởi điểm.");
                return;
            }

            // 3. Ép kiểu và kiểm tra logic số âm
            double price = Double.parseDouble(priceText);
            if (price < 0) {
                showAlert("Lỗi logic", "Giá khởi điểm không được là số âm.");
                return;
            }

            // 4. Kiểm tra lỗi trùng lặp ID (Ngăn chặn tạo 2 sản phẩm trùng ID)
            for (Item existingItem : itemList) {
                if (existingItem.getId().equals(id)) {
                    showAlert("Lỗi trùng lặp", "Mã ID này đã tồn tại. Vui lòng nhập ID khác.");
                    return;
                }
            }

            Item newItem = null;

            // Gọi Factory dựa trên loại hàng
            if ("ELECTRONICS".equals(type)) {
                newItem = ItemFactory.createItem(type, id, name, desc, price,
                        txtElec1.getText(), txtElec2.getText(), txtElec3.getText(), txtElec4.getText());
            } else if ("ART".equals(type)) {
                newItem = ItemFactory.createItem(type, id, name, desc, price,
                        txtArt1.getText(), txtArt2.getText(), txtArt3.getText());
            } else if ("VEHICLE".equals(type)) {
                newItem = ItemFactory.createItem(type, id, name, desc, price,
                        txtVeh1.getText(), txtVeh2.getText(), txtVeh3.getText());
            }

            if (newItem != null) {
                itemList.add(newItem);
                // Lưu lại toàn bộ danh sách vào CSV thông qua DAO
                ItemDAO.saveItems(new ArrayList<>(itemList));
                clearFields();

                // Hiển thị thông báo thành công
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Thành công");
                successAlert.setHeaderText(null);
                successAlert.setContentText("Đã thêm sản phẩm thành công!");
                successAlert.showAndWait();
            }

        } catch (NumberFormatException e) {
            // 5. Bắt lỗi nhập chữ vào ô chỉ cho phép nhập số (Giá tiền, Năm sáng tác, Số dặm)
            showAlert("Sai định dạng số", "Giá khởi điểm hoặc các thông số kỹ thuật (Năm sáng tác, Số dặm) phải là số hợp lệ.");
        } catch (Exception e) {
            // Bắt các lỗi hệ thống không lường trước được
            showAlert("Lỗi hệ thống", "Đã xảy ra sự cố: " + e.getMessage());
        }
    }

    @FXML
    public void handleDelete() {
        Item selected = tableItems.getSelectionModel().getSelectedItem();
        if (selected != null) {
            itemList.remove(selected);
            ItemDAO.saveItems(new ArrayList<>(itemList));
        } else {
            showAlert("Thông báo", "Vui lòng chọn một dòng để xóa.");
        }
    }

    private void clearFields() {
        txtId.clear(); txtName.clear(); txtDescription.clear(); txtInitPrice.clear();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
