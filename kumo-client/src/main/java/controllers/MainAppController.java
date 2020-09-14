package controllers;

import javafx.application.Platform;
import utils.FileInfo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import utils.FileService;

import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class MainAppController implements Initializable {
    @FXML
    TextField serverPathToFile;
    @FXML
    TextField clientPathToFile;
    @FXML
    TableView<FileInfo> clientFilesTable;
    @FXML
    TableView<FileInfo> serverFilesTable;
    @FXML
    ComboBox<String> diskListComboBox;

    private final FileService fileService = new FileService();


    @Override
    public void initialize(URL location, ResourceBundle resources) {
       HelperGui.prepareFileTable(clientFilesTable);
       HelperGui.prepareComboBox(diskListComboBox);
       HelperGui.prepareFileTable(serverFilesTable);
        clientFilesTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                enterToDirectory();
            }
        });
        clientFilesTable.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                enterToDirectory();
            } else if (event.getCode() == KeyCode.BACK_SPACE) {
                upPathDirectory();
            } else if (event.getCode() == KeyCode.C && event.isControlDown()) {
                copy();
            } else if (event.getCode() == KeyCode.V && event.isControlDown()) {
                paste();
            } else if (event.getCode() == KeyCode.DELETE) {
                delete();
            }
        });
        updateFilesList(Paths.get(System.getProperty("user.home")));
    }


    public String getSelectedFileName() {
        if (!clientFilesTable.isFocused()) {
            return null;
        }
        return clientFilesTable.getSelectionModel().getSelectedItem().getFileName();
    }

    public String getCurrentPath() {
        return clientPathToFile.getText();
    }

    public void selectDiskOnAction(ActionEvent actionEvent) {
        ComboBox<String> element = (ComboBox<String>) actionEvent.getSource();
        updateFilesList(Paths.get(element.getSelectionModel().getSelectedItem()));
    }

    // Обновление списка файлов
    public void updateFilesList(Path path) {
        try {
            clientPathToFile.setText(path.normalize().toAbsolutePath().toString());
            clientFilesTable.getItems().clear();
            clientFilesTable.getItems().addAll(Files.list(path)
                    .map(FileInfo::new)
                    .collect(Collectors.toList()));
            clientFilesTable.sort();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Unable to get a list of files. Check file availability and app permissions.", ButtonType.OK);
            alert.showAndWait();
        }
    }

    // Вернуться на папку выше
    private void upPathDirectory() {
        Path upPath = Paths.get(clientPathToFile.getText()).getParent();
        if (upPath != null) {
            updateFilesList(upPath);
        }
    }

    public void btnUpPathDirectoryOnAction(ActionEvent actionEvent) {
        upPathDirectory();
    }

    // Перемещение в выбранную директорию
    public void enterToDirectory() {
        Path path = Paths.get(clientPathToFile.getText())
                .resolve(clientFilesTable
                        .getSelectionModel()
                        .getSelectedItem()
                        .getFileName());
        if (Files.isDirectory(path)) {
            updateFilesList(path);
        }
    }

    public Path getSelectedFile() {
        return Paths.get(getCurrentPath(), getSelectedFileName());
    }

    // Удаление
    private void delete()  {
        try {
            fileService.deleteFile(getSelectedFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
        updateFilesList(Paths.get(getCurrentPath()));
    }

    public void btnDeleteFile(ActionEvent actionEvent) throws IOException {
        delete();
    }

    // Переименование
    private void rename() throws IOException {
        TextInputDialog dialog = new TextInputDialog(getSelectedFileName());
        dialog.setTitle("Rename file");
        dialog.setHeaderText("Enter a new filename");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !(result.get().equals(getSelectedFileName()))) {
            fileService.renameFile(getSelectedFile(), result.get());
        }
        updateFilesList(Paths.get(getCurrentPath()));
    }

    public void btnRenameFile(ActionEvent actionEvent) throws IOException {
        rename();
    }

    // Копирование
    private void copy() {
        fileService.copyFile(getSelectedFile());
    }

    public void btnCopy(ActionEvent actionEvent) {
        copy();
    }

    // Вставка
    private void paste() {
        try {
            fileService.pasteFile(Paths.get(getCurrentPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        updateFilesList(Paths.get(getCurrentPath()));
    }


    public void btnPaste(ActionEvent actionEvent) {
        paste();
    }

    // Перемещение. Сначала надо нажать скопировать чтобы поместить файл или папку в буфер.
    private void move()  {
        try {
            fileService.move(Paths.get(getCurrentPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        updateFilesList(Paths.get(getCurrentPath()));
    }


    public void btnMove(ActionEvent actionEvent) {
        move();
    }


    public void btnExitOnAction(ActionEvent actionEvent) {
        Platform.exit();
    }

    public void btnShowHelp(ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Help");
        alert.setHeaderText("Hotkeys");
        alert.setContentText(
                "Ctrl + c : copy\n" +
                        "Ctrl + v : paste\n" +
                        "Enter : Enter to directory\n" +
                        "Delete : Delete file or directory\n" +
                        "Backspace: Enter to upper directory");
        alert.showAndWait();
    }
}
