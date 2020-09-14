package utils;

import network.Network;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class FileService {
    private Path tmpFile = null;

    private Network network;
    private byte[] bytebuffer = new byte[4096];


    // Загрузка файла на сервер
    public void uploadFile(Path path) {

    }

    // Загрузка файла с сервера
    public void downloadFile() {

    }

    // Удаление файла
    public void deleteFile(Path path) throws IOException {
        Files.delete(path);
    }

    // Переименование файла
    public void renameFile(Path path, String newFileName) throws IOException {
        Path renameTo = path.resolve(newFileName);
        Files.move(path, renameTo, StandardCopyOption.REPLACE_EXISTING);
    }

    // Копирование файла
    public void copyFile(Path path) {
        tmpFile = path;
    }

    // Вставка файла
    public void pasteFile(Path path) throws IOException {
        if (tmpFile != null) {
            Files.copy(tmpFile, path);
        }
    }

    // Перемещение файла
    public void move(Path path) throws IOException {
        Files.move(tmpFile, path);
    }
}
