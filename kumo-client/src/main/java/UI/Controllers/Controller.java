package UI.Controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Controller{
    @FXML
    VBox leftFileTablePane, rightFileTablePane;


    public void btnExitOnAction(ActionEvent actionEvent) {
        Platform.exit();
    }

    public void btnDeleteOnAction(ActionEvent event) {

    }

    public void btnShowHelp(ActionEvent actionEvent) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Help");
        alert.setHeaderText("Hotkeys");
        alert.setContentText(
                "Ctrl + c : copy\n" +
                "Ctrl + v : paste\n" +
                "Enter : Enter to directory\n" +
                "Backspace: Enter to upper directory");
        alert.showAndWait();
    }
}
