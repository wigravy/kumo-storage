package UI.Controllers;

import network.Network;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import lombok.Setter;


public class Controller{
    @Setter
    private Network network;

    public void btnExitOnAction(ActionEvent actionEvent) {
        Platform.exit();
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
