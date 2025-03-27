package com.example.banksys;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class BankSys extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(BankSys.class.getResource("/com/example/banksys/hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 600);
        stage.setTitle("Paskolos skaičiuoklė");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}