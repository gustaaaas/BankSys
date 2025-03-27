package com.example.banksys;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class TopMenu {
    @FXML
    protected void onPaskolosSumButtonClick() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/banksys/PaskolosSkaiciuokle.fxml"));
            Parent root = fxmlLoader.load();

            Stage newStage = new Stage();
            newStage.setTitle("Paskolos Skaičiuoklė");
            newStage.setScene(new Scene(root));
            newStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}