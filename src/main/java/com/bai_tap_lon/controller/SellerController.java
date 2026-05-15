package com.bai_tap_lon.controller;
//File này định nghĩa các tham chiếu, phương thức để gán vào các ô hợp lí trong file giao  diện fxml.
import com.bai_tap_lon.dao.ItemDAO;
import com.bai_tap_lon.factory.ItemFactory;
import com.bai_tap_lon.model.Item;
import javafx.animation.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.scene.control.TableCell;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;


public class SellerController implements Initializable {

    // ─── FXML bindings ───────────────────────────────────────────────
    @FXML private TextField txtId, txtName, txtInitPrice;
    @FXML private TextArea  txtDescription;
    @FXML private ComboBox<String> cbCategory;
    @FXML private VBox paneDynamicFields;

    @FXML private VBox vboxElectronics, vboxArt, vboxVehicle, vboxFashion, vboxFurniture;
    @FXML private TextField txtElec1, txtElec2, txtElec3, txtElec4;
    @FXML private TextField txtArt1,  txtArt2,  txtArt3;
    @FXML private TextField txtVeh1,  txtVeh2,  txtVeh3;
    @FXML private TextField txtFas1,  txtFas2,  txtFas3;
    @FXML private TextField txtFur1,  txtFur2;

    @FXML private TableView<Item>           tableItems;
    @FXML private TableColumn<Item, String> colId, colName, colCategory, colDesc;
    @FXML private TableColumn<Item, Double> colPrice;
    @FXML private Button btnAdd, btnDelete;
    @FXML private VBox   formPanel;
    @FXML private Label  lblCount;

    private final ObservableList<Item> itemList = FXCollections.observableArrayList();

    // ─── Initialize ──────────────────────────────────────────────────
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        setupComboBox();
        setupButtonEffects();
        playEntranceAnimation();
    }

    // ─── Table ───────────────────────────────────────────────────────
    private void setupTable() {
        // Định dạng giá: 23000000 → 23,000,000
        colPrice.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("%,.0f", price));
                }
            }
        });
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("initPrice"));
        colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));

        itemList.addAll(ItemDAO.loadItems());
        tableItems.setItems(itemList);
        updateItemCountBadge();
    }

    private void setupComboBox() {
        cbCategory.getItems().addAll("ELECTRONICS", "ART", "VEHICLE", "FASHION", "FURNITURE");
    }

    // ─── Entrance: stagger fade + slide ──────────────────────────────
    private void playEntranceAnimation() {
        List<Node> targets = new ArrayList<>();
        targets.add(formPanel);
        targets.add(tableItems);

        for (int i = 0; i < targets.size(); i++) {
            Node node = targets.get(i);
            if (node == null) continue;

            node.setOpacity(0);
            node.setTranslateY(28);
            double delayMs = 80 + i * 130.0;

            FadeTransition fade = new FadeTransition(Duration.millis(560), node);
            fade.setFromValue(0.0);
            fade.setToValue(1.0);
            fade.setInterpolator(Interpolator.EASE_OUT);
            fade.setDelay(Duration.millis(delayMs));

            TranslateTransition slide = new TranslateTransition(Duration.millis(600), node);
            slide.setFromY(28);
            slide.setToY(0);
            slide.setInterpolator(Interpolator.EASE_OUT);
            slide.setDelay(Duration.millis(delayMs));

            fade.play();
            slide.play();
        }
    }

    // ─── Button effects ───────────────────────────────────────────────
    private void setupButtonEffects() {
        setupSpringButton(btnAdd);
        setupSpringButton(btnDelete);
    }

    private void setupSpringButton(Button button) {
        if (button == null) return;

        ScaleTransition hoverIn = new ScaleTransition(Duration.millis(220), button);
        hoverIn.setToX(1.07); hoverIn.setToY(1.07);
        hoverIn.setInterpolator(Interpolator.EASE_OUT);

        ScaleTransition hoverOut = new ScaleTransition(Duration.millis(200), button);
        hoverOut.setToX(1.0); hoverOut.setToY(1.0);
        hoverOut.setInterpolator(Interpolator.EASE_OUT);

        button.setOnMouseEntered(e -> { hoverOut.stop(); hoverIn.playFromStart(); });
        button.setOnMouseExited(e  -> { hoverIn.stop();  hoverOut.playFromStart(); });

        // Press: scale down
        button.setOnMousePressed(e -> {
            hoverIn.stop(); hoverOut.stop();
            ScaleTransition press = new ScaleTransition(Duration.millis(90), button);
            press.setToX(0.93); press.setToY(0.93);
            press.setInterpolator(Interpolator.EASE_IN);
            press.play();
        });

        // Release: bounce back
        button.setOnMouseReleased(e -> {
            ScaleTransition release = new ScaleTransition(Duration.millis(400), button);
            release.setToX(1.0); release.setToY(1.0);
            release.setInterpolator(Interpolator.EASE_BOTH);
            release.play();
        });
    }

    // ─── Category change ──────────────────────────────────────────────
    @FXML
    public void handleCategoryChange() {
        String selected = cbCategory.getValue();

        FadeTransition fadeOut = new FadeTransition(Duration.millis(130), paneDynamicFields);
        fadeOut.setFromValue(paneDynamicFields.getOpacity());
        fadeOut.setToValue(0.0);
        fadeOut.setInterpolator(Interpolator.EASE_IN);

        TranslateTransition slideOut = new TranslateTransition(Duration.millis(130), paneDynamicFields);
        slideOut.setToY(-8);
        slideOut.setInterpolator(Interpolator.EASE_IN);

        ParallelTransition exitAnim = new ParallelTransition(fadeOut, slideOut);
        exitAnim.setOnFinished(e -> {
            hideAll();

            switch (selected != null ? selected : "") {
                case "ELECTRONICS" -> showSubPanel(vboxElectronics);
                case "ART"         -> showSubPanel(vboxArt);
                case "VEHICLE"     -> showSubPanel(vboxVehicle);
                case "FASHION"     -> showSubPanel(vboxFashion);
                case "FURNITURE"   -> showSubPanel(vboxFurniture);
            }

            paneDynamicFields.setTranslateY(14);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(350), paneDynamicFields);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.setInterpolator(Interpolator.EASE_OUT);

            TranslateTransition slideIn = new TranslateTransition(Duration.millis(380), paneDynamicFields);
            slideIn.setFromY(14);
            slideIn.setToY(0);
            slideIn.setInterpolator(Interpolator.EASE_OUT);

            new ParallelTransition(fadeIn, slideIn).play();
        });

        exitAnim.play();
    }

    private void hideAll() {
        for (VBox v : List.of(vboxElectronics, vboxArt, vboxVehicle, vboxFashion, vboxFurniture)) {
            v.setVisible(false);
            v.setManaged(false);
        }
    }

    private void showSubPanel(VBox panel) {
        panel.setVisible(true);
        panel.setManaged(true);
    }

    // ─── Handle Add ──────────────────────────────────────────────────
    @FXML
    public void handleAdd() {
        try {
            String type = cbCategory.getValue();
            if (type == null || type.trim().isEmpty()) {
                showError("Lỗi nhập liệu", "Vui lòng chọn loại hàng hóa.");
                return;
            }

            String id        = txtId.getText().trim().replace(",", "-");
            String name      = txtName.getText().trim().replace(",", "-");
            String desc      = txtDescription.getText().trim().replace(",", "-");
            String priceText = txtInitPrice.getText().trim();

            if (id.isEmpty() || name.isEmpty() || priceText.isEmpty()) {
                showError("Lỗi nhập liệu", "Vui lòng nhập đầy đủ ID, Tên và Giá khởi điểm.");
                return;
            }

            double price = Double.parseDouble(priceText);
            if (price < 0) {
                showError("Lỗi logic", "Giá khởi điểm không được âm.");
                return;
            }

            for (Item existing : itemList) {
                if (existing.getId().equals(id)) {
                    showError("Lỗi trùng lặp", "Mã ID \"" + id + "\" đã tồn tại.");
                    shakeNode(txtId);
                    return;
                }
            }

            Item newItem = buildItem(type, id, name, desc, price);

            if (newItem != null) {
                itemList.add(newItem);
                ItemDAO.saveItems(new ArrayList<>(itemList));
                updateItemCountBadge();
                clearFields();
                playSuccessPulse(btnAdd);
                showSuccess("Thêm thành công!", "Sản phẩm \"" + name + "\" đã được thêm vào kho.");
            }

        } catch (NumberFormatException e) {
            showError("Sai định dạng số", "Giá và thông số kỹ thuật phải là số hợp lệ.");
            shakeNode(txtInitPrice);
        } catch (Exception e) {
            showError("Lỗi hệ thống", "Đã xảy ra lỗi: " + e.getMessage());
        }
    }

    private Item buildItem(String type, String id, String name, String desc, double price) {
        return switch (type) {
            case "ELECTRONICS" -> ItemFactory.createItem(type, id, name, desc, price,
                    txtElec1.getText(), txtElec2.getText(), txtElec3.getText(), txtElec4.getText());
            case "ART" -> {
                String yr = txtArt2.getText().trim().isEmpty() ? "0" : txtArt2.getText().trim();
                yield ItemFactory.createItem(type, id, name, desc, price,
                        txtArt1.getText(), yr, txtArt3.getText());
            }
            case "VEHICLE" -> {
                String mi = txtVeh3.getText().trim().isEmpty() ? "0" : txtVeh3.getText().trim();
                yield ItemFactory.createItem(type, id, name, desc, price,
                        txtVeh1.getText(), txtVeh2.getText(), mi);
            }
            case "FASHION"   -> ItemFactory.createItem(type, id, name, desc, price,
                    txtFas1.getText(), txtFas2.getText(), txtFas3.getText());
            case "FURNITURE" -> ItemFactory.createItem(type, id, name, desc, price,
                    txtFur1.getText(), txtFur2.getText());
            default -> null;
        };
    }

    // ─── Handle Delete ───────────────────────────────────────────────
    @FXML
    public void handleDelete() {
        Item selected = tableItems.getSelectionModel().getSelectedItem();
        if (selected != null) {
            itemList.remove(selected);
            ItemDAO.saveItems(new ArrayList<>(itemList));
            updateItemCountBadge();
        } else {
            showError("Thông báo", "Vui lòng chọn một mặt hàng để xoá.");
            shakeNode(tableItems);
        }
    }

    // ─── Micro-animations ────────────────────────────────────────────

    private void shakeNode(Node node) {
        TranslateTransition shake = new TranslateTransition(Duration.millis(55), node);
        shake.setFromX(0);
        shake.setToX(7);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.setInterpolator(Interpolator.EASE_BOTH);
        shake.setOnFinished(e -> node.setTranslateX(0));
        shake.play();
    }

    private void playSuccessPulse(Button button) {
        ScaleTransition pulse = new ScaleTransition(Duration.millis(180), button);
        pulse.setToX(1.12); pulse.setToY(1.12);
        pulse.setInterpolator(Interpolator.EASE_OUT);

        ScaleTransition settle = new ScaleTransition(Duration.millis(320), button);
        settle.setToX(1.0); settle.setToY(1.0);
        settle.setInterpolator(Interpolator.EASE_BOTH);

        pulse.setOnFinished(e -> settle.play());
        pulse.play();
    }

    // ─── Helpers ─────────────────────────────────────────────────────
    private void updateItemCountBadge() {
        if (lblCount != null) {
            lblCount.setText(itemList.size() + " SẢN PHẨM");
        }
    }

    private void clearFields() {
        txtId.clear(); txtName.clear(); txtDescription.clear(); txtInitPrice.clear();
        txtElec1.clear(); txtElec2.clear(); txtElec3.clear(); txtElec4.clear();
        txtArt1.clear();  txtArt2.clear();  txtArt3.clear();
        txtVeh1.clear();  txtVeh2.clear();  txtVeh3.clear();
        txtFas1.clear();  txtFas2.clear();  txtFas3.clear();
        txtFur1.clear();  txtFur2.clear();
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        styleAlert(alert);
        alert.showAndWait();
    }

    private void showSuccess(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        styleAlert(alert);
        alert.showAndWait();
    }

    private void styleAlert(Alert alert) {
        try {
            alert.getDialogPane().getScene().getRoot()
                    .getStylesheets()
                    .add(getClass().getResource("/styles/style.css").toExternalForm());
        } catch (Exception ignored) {}
    }
}