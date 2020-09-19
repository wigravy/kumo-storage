package com.wigravy.kumoStorage.common.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.FileRegion;
import io.netty.util.concurrent.FutureListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileService {
    private FileRegion fileRegion;
    private ByteBuf buffer;
    private byte[] filenameBytes;
    private Path tmpFile = null;

    // Отправка файла. Оптимизировано: размер файлового буфера генерируется в самом начале под размер всех последующих команд.

    public void uploadFile(Channel channel, Path path, FutureListener listener) {
        try {

            fileRegion = new DefaultFileRegion(new FileInputStream(path.toFile()).getChannel(), 0, Files.size(path));
            filenameBytes = path.getFileName().toString().getBytes(StandardCharsets.UTF_8);
            buffer = ByteBufAllocator.DEFAULT.directBuffer(1 + 4 + filenameBytes.length + 8);


            // Запись сигнального байта - номера команды
            buffer.writeByte(ListSignalBytes.FILE_SIGNAL_BYTE);

            // Запись длинны имени файла
            buffer.writeInt(path.getFileName().toString().length());

            // Запись имени файла
            buffer.writeBytes(filenameBytes);


            // Запись размера файла. Отправка получившейся команды

            buffer.writeLong(Files.size(path));
            // todo удалить или залогировать
            System.out.println("Путь до файла на клиенте: " + path.toString());
            System.out.println("Длинна имени файла: " + filenameBytes.length + " байт");
            System.out.println("Общий размер файла: " + Files.size(path) + " байт");
            System.out.println("Размер буфера: " + buffer.readableBytes());
            channel.writeAndFlush(buffer);

            // Отправка файла
            ChannelFuture future = channel.writeAndFlush(fileRegion);
            if (listener != null) {
                future.addListener(listener);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //  Отправка комманд
    public void sendCommand(Channel channel, String command) {
        // Отправка сигнального байта - номера команды
        buffer = ByteBufAllocator.DEFAULT.directBuffer(1 + 4 + command.length());
        // Запись сигнального байта - номера команды
        buffer.writeByte(ListSignalBytes.CMD_SIGNAL_BYTE);
        // Запись длинны команды
        buffer.writeInt(command.length());
        // Запись команды
        buffer.writeBytes(command.getBytes());
        channel.writeAndFlush(buffer);
    }

    // Удаление файла
    public void deleteFile(Path path) throws IOException {
        Files.delete(path);
    }

    // Переименование файла
    public void renameFile(Path path, String newFileName) throws IOException {
        Path renameTo = path.resolveSibling(newFileName);
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

    public List<FileInfo> createFileList(String s) {
        List<FileInfo> temp = new ArrayList<>();
        String[] files = s.split("\n");
        for (String file : files) {
            String[] tmp = file.split(",");
            FileInfo fileInfo = new FileInfo();
            fileInfo.setFileName(tmp[0]);
            fileInfo.setSize(Long.parseLong(tmp[1]));
            if (tmp[2].equals("FILE")) {
                fileInfo.setFileType(FileType.FILE);
            } else {
                fileInfo.setFileType(FileType.DIRECTORY);
            }
            fileInfo.setLastModified(LocalDateTime.parse(tmp[3]));
            temp.add(fileInfo);
        }
        return temp;
    }
}

