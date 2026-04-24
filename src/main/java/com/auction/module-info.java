module com.auction {
    requires javafx.controls;
    requires javafx.fxml;

    exports com.auction;
    exports com.auction.client;
    exports com.auction.client.controller;
    exports com.auction.common.models;
    exports com.auction.common.util;
    exports com.auction.domain;

    opens com.auction.client to javafx.fxml;
    opens com.auction.client.controller to javafx.fxml;
}
