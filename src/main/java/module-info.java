open module com.icesi {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.json;
    requires org.junit.jupiter.api;

    exports com.icesi;
    exports com.icesi.controller;
    exports com.icesi.model;
    exports com.icesi.structures;
    exports com.icesi.exceptions;
    exports com.icesi.persistence;
    exports com.icesi.ui;
}
