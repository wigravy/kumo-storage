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
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ClientFileTable implements Initializable {
    @FXML
    TextField pathToFileTextField;

    @FXML
    TableView<FileInfo> filesTableView;

    @FXML
    ComboBox<String> diskListComboBox;

    Path pathToCopyFile;


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
                upPathDirectory();
            } else if (event.getCode() == KeyCode.SPACE) {
                //TODO: множественное выделение объектов
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

    // Обновление списка файлов
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

    // Вернуться на папку выше
    private void upPathDirectory() {
        Path upPath = Paths.get(pathToFileTextField.getText()).getParent();
        if (upPath != null) {
            updateFilesList(upPath);
        }
    }

    public void btnUpPathDirectoryOnAction(ActionEvent actionEvent) {
        upPathDirectory();
    }

    // Перемещение в выбранную директорию
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

    // Удаление файла
    private void delete() {
        Path pathToFile = Paths.get(getCurrentPath(), getSelectedFileName());
        try {
            Files.delete(pathToFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        updateFilesList(Paths.get(getCurrentPath()));
    }

    public void btnDeleteFile(ActionEvent actionEvent) {
        delete();
    }

    // Переименование
    private void rename() {
        Path pathToFile = Paths.get(getCurrentPath(), getSelectedFileName());
        TextInputDialog dialog = new TextInputDialog(getSelectedFileName());
        dialog.setTitle("Rename file");
        dialog.setHeaderText("Enter a new filename");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !(result.get().equals(getSelectedFileName()))) {
            Path newName = Paths.get(getCurrentPath(), result.get());
            try {
                Files.move(pathToFile, newName, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        updateFilesList(Paths.get(getCurrentPath()));
    }

    public void btnRenameFile(ActionEvent actionEvent) {
        rename();
    }

    // Копирование
    private void copy() {
        pathToCopyFile = Paths.get(getCurrentPath(), getSelectedFileName());
    }

    public void btnCopy(ActionEvent actionEvent) {
        copy();
    }

    // Вставка
    private void paste() {
        if (pathToCopyFile != null && !(getCurrentPath().equals(pathToCopyFile))) {
            Path dst = Paths.get(getCurrentPath()).resolve(pathToCopyFile.getFileName().toString());
            try {
                Files.copy(pathToCopyFile, dst);
            } catch (IOException e) {
                e.printStackTrace();
            }
            updateFilesList(Paths.get(getCurrentPath()));
        }
    }

    public void btnPaste(ActionEvent actionEvent) {
        paste();
    }

    // Перемещение. Сначала надо нажать скопировать чтобы поместить файл или папку в буфер.
    private void move() {
        if (pathToCopyFile != null) {
            Path dst = Paths.get(getCurrentPath()).resolve(pathToCopyFile.getFileName().toString());
            try {
                Files.move(pathToCopyFile, dst);
            } catch (IOException e) {
                e.printStackTrace();
            }
            updateFilesList(Paths.get(getCurrentPath()));
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Before moving, you must select the file. This can be done with the command 'Copy'", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void btnMove(ActionEvent actionEvent) {
        move();
    }
}
