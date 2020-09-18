package com.wigravy.kumoStorage.client.controllers;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import com.wigravy.kumoStorage.common.utils.FileInfo;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

/*
 ** Код для инициализации таблиц с файлами Сервера и Клиента.
 */
public class HelperGui {
    public static void prepareFileTable(TableView<FileInfo> tableView) {
        TableColumn<FileInfo, String> fileTypeColumn = new TableColumn<>("");
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
                    String text = FileInfo.getStringSize(item);
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
        fileLastUpdateColumn.setMinWidth(200);

        tableView.getSortOrder().add(fileTypeColumn);
        tableView.getColumns().addAll(fileTypeColumn, fileNameColumn, fileSizeColumn, fileLastUpdateColumn);

        tableView.getItems().clear();
        tableView.getSelectionModel().select(0);
    }

    public static void prepareComboBox(ComboBox<String> comboBox) {
        for (File file : File.listRoots()) {
            comboBox.getItems().add(file.getName());
        }

    }

}
