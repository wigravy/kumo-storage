package com.wigravy.kumoStorage.client.controllers;

import com.wigravy.kumoStorage.client.main.ClientApp;
import com.wigravy.kumoStorage.client.network.Network;
import com.wigravy.kumoStorage.common.utils.ServiceMessage;
import javafx.application.Platform;
import com.wigravy.kumoStorage.common.utils.FileInfo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import com.wigravy.kumoStorage.common.utils.FileService;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MainAppController implements Initializable {
    private Network network = Network.getInstance();
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
    private List<FileInfo> serverFileList;


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
        Thread t = new Thread(() -> {
            network.getMainHandler().setServiceCallback(serviceMsg -> {
                System.out.println(serviceMsg);
                if (serviceMsg.startsWith("/FileList ")) {
                    serverFileList = FileService.createFileList(serviceMsg.split(" ", 2)[1]);
                    Platform.runLater(() -> {
                        serverFilesTable.getItems().clear();
                        serverFileList.forEach(o -> serverFilesTable.getItems().add(o));
                    });
                    clientFilesTable.sort();
                }
            });
        });
        t.start();
        updateFilesList(Paths.get(System.getProperty("user.home")));
        updateServerFileList();
    }


    public void selectDiskOnAction(ActionEvent actionEvent) {
        ComboBox<String> element = (ComboBox<String>) actionEvent.getSource();
        updateFilesList(Paths.get(element.getSelectionModel().getSelectedItem()));
    }

    public void updateServerFileList() {
        FileService.sendCommand(network.getChannel(), "/updateFileList");
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

    public String getSelectedFileName() {
        if (clientFilesTable.isFocused()) {
            return clientFilesTable.getSelectionModel().getSelectedItem().getFileName();
        } else if (serverFilesTable.isFocused()) {
            return serverFilesTable.getSelectionModel().getSelectedItem().getFileName();
        } else {
            throw new NullPointerException("No one file selected");
        }
    }

    public String getCurrentPath() {
        if (serverFilesTable.isFocused()) {
            return "";
        } else  {
            return clientPathToFile.getText();
        }
    }

    public Path getSelectedFile() {
        return Paths.get(getCurrentPath(), getSelectedFileName());
    }

    // Удаление
    private void delete() {
        if (serverFilesTable.isFocused()) {
            FileService.sendCommand(network.getChannel(), "/delete " + getSelectedFileName());
        } else if (clientFilesTable.isFocused()) {
            try {
                FileService.deleteFile(getSelectedFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
            updateFilesList(Paths.get(getCurrentPath()));
        }
    }

    public void btnDeleteFile(ActionEvent actionEvent) throws IOException {
        delete();
    }

    // Переименование
    private void rename() throws IOException {
        if (serverFilesTable.isFocused()) {
            String oldFilename = getSelectedFileName();
            TextInputDialog dialog = new TextInputDialog(getSelectedFileName());
            dialog.setTitle("Rename file");
            dialog.setHeaderText("Enter a new filename");
            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                FileService.sendCommand(network.getChannel(), "/rename " + oldFilename + " " + result.get());
            }

        } else if (clientFilesTable.isFocused()) {
            Path oldFile = getSelectedFile();
            TextInputDialog dialog = new TextInputDialog(getSelectedFileName());
            dialog.setTitle("Rename file");
            dialog.setHeaderText("Enter a new filename");
            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                FileService.renameFile(oldFile, result.get());
            }
            updateFilesList(Paths.get(getCurrentPath()));
        }
    }

    public void btnRenameFile(ActionEvent actionEvent) throws IOException {
        rename();
    }

    // Копирование
    private void copy() {
        FileService.copyFile(getSelectedFile());
    }

    public void btnCopy(ActionEvent actionEvent) {
        copy();
    }

    // Вставка
    private void paste() {
        try {
            FileService.pasteFile(Paths.get(getCurrentPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        updateFilesList(Paths.get(getCurrentPath()));
    }


    public void btnPaste(ActionEvent actionEvent) {
        paste();
    }

    // Перемещение. Сначала надо нажать скопировать чтобы поместить файл или папку в буфер.
    private void move() {
        try {
            FileService.move(Paths.get(getCurrentPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        updateFilesList(Paths.get(getCurrentPath()));
    }


    public void btnMove(ActionEvent actionEvent) {
        move();
    }


    public void btnExitOnAction(ActionEvent actionEvent) {
        network.close();
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


    public void btnRefreshClientFileList(ActionEvent actionEvent) {
        updateFilesList(Paths.get(getCurrentPath()));
    }

    public void btnRefreshServerFileList(ActionEvent actionEvent) {
        updateServerFileList();
    }


    public void btnUpload(ActionEvent actionEvent) throws Exception {
        FileService.uploadFile(network.getChannel(), getSelectedFile(), null);
    }

    public void btnDownload(ActionEvent actionEvent) {
        network.getMainHandler().setCurrentPath(Path.of(getCurrentPath()));
        FileService.sendCommand(network.getChannel(), "/download " + getSelectedFileName());
    }
}
