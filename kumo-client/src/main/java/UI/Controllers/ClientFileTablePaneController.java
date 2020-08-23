package UI.Controllers;

import Utils.FileInfo;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;

import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ClientFileTablePaneController implements Initializable {
    @FXML
    TextField pathToFileTextField;

    @FXML
    TableView<FileInfo> filesTableView;

    @FXML
    ComboBox<String> diskListComboBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        TableColumn<FileInfo, String> fileTypeColumn = new TableColumn<>();
        fileTypeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFileType().getType()));
        fileTypeColumn.setMinWidth(5);

        TableColumn<FileInfo, String> fileNameColumn = new TableColumn<>("Name");
        fileNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFileName()));
        fileNameColumn.setMinWidth(250);

        TableColumn<FileInfo, Long> fileSizeColumn = new TableColumn<>("Size");
        fileSizeColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSize()));
        fileSizeColumn.setMinWidth(200);
        fileSizeColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    String text = String.format("%,d bytes", item);
                    if (item == -1L) {
                        text = "[DIR]";
                    }
                    setText(text);
                }
            }
        });

        TableColumn<FileInfo, String> fileLastUpdateColumn = new TableColumn<>("Last update");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        fileLastUpdateColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getLastModified().format(dateTimeFormatter)));
        fileLastUpdateColumn.setMinWidth(150);

        filesTableView.getSortOrder().add(fileTypeColumn);
        filesTableView.getColumns().addAll(fileTypeColumn, fileNameColumn, fileSizeColumn, fileLastUpdateColumn);

        diskListComboBox.getItems().clear();
        for (Path path : FileSystems.getDefault().getRootDirectories()) {
            diskListComboBox.getItems().add(path.toString());
        }
        diskListComboBox.getSelectionModel().select(0);

        filesTableView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                enterToDirectory();
            }
        });

        filesTableView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                enterToDirectory();
            } else if (event.getCode() == KeyCode.BACK_SPACE) {
                Path upPath = Paths.get(pathToFileTextField.getText()).getParent();
                if (upPath != null) {
                    updateFilesList(upPath);
                }
            } else if (event.getCode() == KeyCode.SPACE) {
               //TODO: множественное выделение объектов
            }
        });

        updateFilesList(Paths.get(System.getProperty("user.home")));
    }



    public void enterToDirectory() {
        Path path = Paths.get(pathToFileTextField.getText())
                .resolve(filesTableView
                        .getSelectionModel()
                        .getSelectedItem()
                        .getFileName());
        if (Files.isDirectory(path)) {
            updateFilesList(path);
        }
    }


    public void updateFilesList(Path path) {
        try {
            pathToFileTextField.setText(path.normalize().toAbsolutePath().toString());
            filesTableView.getItems().clear();
            filesTableView.getItems().addAll(Files.list(path)
                    .map(FileInfo::new)
                    .collect(Collectors.toList()));
            filesTableView.sort();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Unable to get a list of files. Check file availability and app permissions.", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void btnUpPathDirectoryOnAction(ActionEvent actionEvent) {
        Path upPath = Paths.get(pathToFileTextField.getText()).getParent();
        if (upPath != null) {
            updateFilesList(upPath);
        }
    }

    public String getSelectedFileName() {
        if (!filesTableView.isFocused()) {
            return null;
        }
        return filesTableView.getSelectionModel().getSelectedItem().getFileName();
    }

    public String getCurrentPath() {
        return pathToFileTextField.getText();
    }

    public void selectDiskOnAction(ActionEvent actionEvent) {
        ComboBox<String> element = (ComboBox<String>) actionEvent.getSource();
        updateFilesList(Paths.get(element.getSelectionModel().getSelectedItem()));
    }
}
