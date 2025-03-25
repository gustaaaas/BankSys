module com.example.banksys {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.dlsc.formsfx;
    requires com.github.librepdf.openpdf;
    requires java.desktop;
    opens com.example.banksys to javafx.fxml;
    exports com.example.banksys;
}