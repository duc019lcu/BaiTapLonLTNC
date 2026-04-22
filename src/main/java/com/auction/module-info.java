module com.auction.onlineauctionsystem {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.auction.onlineauctionsystem to javafx.fxml;
    opens com.auction.onlineauctionsystem.controller to javafx.fxml; // Cho phép FXML truy cập Controller

    exports com.auction.onlineauctionsystem;
}
