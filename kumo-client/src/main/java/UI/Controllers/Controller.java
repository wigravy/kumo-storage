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


    public void btnCopyOnAction(ActionEvent actionEvent) {
        ClientFileTablePaneController serverFileTable = (ClientFileTablePaneController) leftFileTablePane.getProperties().get("ctrl");
        ClientFileTablePaneController clientFileTable = (ClientFileTablePaneController) rightFileTablePane.getProperties().get("ctrl");
        ClientFileTablePaneController src, dst;
        if (serverFileTable.getSelectedFileName() == null && clientFileTable.getSelectedFileName() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "First you need to select a file!", ButtonType.OK);
            alert.showAndWait();
            return;
        } else {
            if (serverFileTable.getSelectedFileName() != null) {
                src = serverFileTable;
                dst = clientFileTable;
            } else {
                src = clientFileTable;
                dst = serverFileTable;
            }
        }
        Path srcPath = Paths.get(src.getCurrentPath(), src.getSelectedFileName());
        Path dstPath = Paths.get(dst.getCurrentPath()).resolve(srcPath.getFileName().toString());

        try {
            Files.copy(srcPath, dstPath);
            dst.updateFilesList(Paths.get(dst.getCurrentPath()));
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "File already exist", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void btnDeleteOnAction(ActionEvent event) {

    }
}
