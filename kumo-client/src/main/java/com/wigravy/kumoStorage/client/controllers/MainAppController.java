package com.wigravy.kumoStorage.client.controllers;

import com.wigravy.kumoStorage.client.network.Network;
import com.wigravy.kumoStorage.common.utils.FileType;
import javafx.application.Platform;
import com.wigravy.kumoStorage.common.utils.FileInfo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import com.wigravy.kumoStorage.common.utils.FileService;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
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
    FileService fileService = new FileService();


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        HelperGui.prepareFileTable(clientFilesTable);
        HelperGui.prepareComboBox(diskListComboBox);
        HelperGui.prepareFileTable(serverFilesTable);
        prepareTableEvents(clientFilesTable);
        prepareTableEvents(serverFilesTable);

        Thread t = new Thread(() -> {
            network.getMainHandler().setCallback(serviceMsg -> {
                if (serviceMsg.startsWith("/FileList")) {
                    if (serviceMsg.split("\n").length != 1) {
                        serverFileList = fileService.createFileList(serviceMsg.split("\n", 2)[1]);
                        Platform.runLater(() -> {
                            serverFilesTable.getItems().clear();
                            serverFileList.forEach(o -> serverFilesTable.getItems().add(o));
                            serverFilesTable.sort();
                        });
                    } else {
                        Platform.runLater(() -> {
                            serverFilesTable.getItems().clear();
                        });
                    }
                } else if (serviceMsg.startsWith("/updateClientFileList")) {
                    updateFilesList(Paths.get(getCurrentPath()));
                }
            });
        });
        t.start();
        updateFilesList(Paths.get(System.getProperty("user.home")));
        updateServerFileList();
    }


    private void prepareTableEvents(TableView<FileInfo> tableView) {
        tableView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                if (tableView.getSelectionModel().getSelectedItem().getFileType() == FileType.DIRECTORY) {
                    enterToDirectory();
                }
            }
        });
        tableView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                enterToDirectory();
            } else if (event.getCode() == KeyCode.DELETE) {
                delete();
            }
        });
    }


    public void selectDiskOnAction(ActionEvent actionEvent) {
        ComboBox<String> element = (ComboBox<String>) actionEvent.getSource();
        updateFilesList(Paths.get(element.getSelectionModel().getSelectedItem()));
    }

    public void updateServerFileList() {
        fileService.sendCommand(network.getChannel(), "/updateFileList");
    }

    public void updateFilesList(Path path) {
        try {
            clientPathToFile.setText(path.normalize().toAbsolutePath().toString());
            clientFilesTable.getItems().clear();
            clientFilesTable.getItems()
                    .addAll(Files.list(path)
                            .map(FileInfo::new)
                            .collect(Collectors.toList()));
            clientFilesTable.sort();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Unable to get a list of files. Check file availability and app permissions.", ButtonType.OK);
            alert.showAndWait();
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
        return clientPathToFile.getText();
    }

    public Path getSelectedFile() {
        return Paths.get(getCurrentPath(), getSelectedFileName());
    }

    private void upClientPathDirectory() {
        Path upPath = Paths.get(clientPathToFile.getText()).getParent();
        if (upPath != null) {
            network.getMainHandler().setCurrentPath(upPath);
            updateFilesList(upPath);
        }
    }

    public void btnUpPathDirectoryOnAction(ActionEvent actionEvent) {
        upClientPathDirectory();
    }

    private void upServerPathDirectory() {
        fileService.sendCommand(network.getChannel(), "/upDirectory");
    }

    public void btnUpServerPathDirectoryOnAction(ActionEvent actionEvent) {
        upServerPathDirectory();
    }

    public void enterToDirectory() {
        if (serverFilesTable.isFocused()) {
            fileService.sendCommand(network.getChannel(), "/enterToDirectory\n" + getSelectedFileName());
        } else {
            Path path = Paths.get(clientPathToFile.getText())
                    .resolve(clientFilesTable
                            .getSelectionModel()
                            .getSelectedItem()
                            .getFileName());
            if (Files.isDirectory(path)) {
                updateFilesList(path);
                network.getMainHandler().setCurrentPath(path);
            }
        }
    }

    private void delete() {
        if (serverFilesTable.isFocused()) {
            fileService.sendCommand(network.getChannel(), "/delete\n" + getSelectedFileName());
        } else if (clientFilesTable.isFocused()) {
            try {
                fileService.delete(getSelectedFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
            updateFilesList(Paths.get(getCurrentPath()));
        }
    }

    public void btnDeleteFile(ActionEvent actionEvent) {
        delete();
    }

    private void rename() throws IOException {
        if (serverFilesTable.isFocused()) {
            String oldFilename = getSelectedFileName();
            TextInputDialog dialog = new TextInputDialog(getSelectedFileName());
            dialog.setTitle("Rename file");
            dialog.setHeaderText("Enter a new filename");
            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                fileService.sendCommand(network.getChannel(), "/rename\n" + oldFilename + "\n" + result.get());
            }

        } else if (clientFilesTable.isFocused()) {
            Path oldFile = getSelectedFile();
            TextInputDialog dialog = new TextInputDialog(getSelectedFileName());
            dialog.setTitle("Rename file");
            dialog.setHeaderText("Enter a new filename");
            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                fileService.rename(oldFile, result.get());
            }
            updateFilesList(Paths.get(getCurrentPath()));
        }
    }

    public void btnRenameFile(ActionEvent actionEvent) throws IOException {
        rename();
    }

    public void btnExitOnAction(ActionEvent actionEvent) {
        network.close();
        Platform.exit();
    }

    public void btnRefreshClientFileList(ActionEvent actionEvent) {
        updateFilesList(Paths.get(getCurrentPath()));
    }

    public void btnRefreshServerFileList(ActionEvent actionEvent) {
        updateServerFileList();
    }

    public void btnDownload(ActionEvent actionEvent) {
        if (serverFilesTable.isFocused()) {
            network.getMainHandler().setCurrentPath(Path.of(getCurrentPath()));
            fileService.sendCommand(network.getChannel(), "/download\n" + getSelectedFileName());
        } else {
            try {
                fileService.uploadFile(network.getChannel(), getSelectedFile(), null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
