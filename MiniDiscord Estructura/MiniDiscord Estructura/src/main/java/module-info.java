module cr.ac.una.est.minidiscord {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.sql;
    requires mysql.connector.j;

    opens cr.ac.una.est.minidiscord.presentation.controller to javafx.fxml;

    exports cr.ac.una.est.minidiscord;
    exports cr.ac.una.est.minidiscord.network;
    exports cr.ac.una.est.minidiscord.presentation.controller;
}
