package com.wigravy.kumoStorage.common.utils;

import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Getter
@Setter
public class FileInfo {
    private String fileName;
    private FileType fileType;
    private long size;
    private LocalDateTime lastModified;

    public FileInfo() {}

    public FileInfo(Path path) {
        try {
            this.fileName = path.getFileName().toString();
            this.fileType = Files.isDirectory(path) ? FileType.DIRECTORY : FileType.FILE;
            this.size = Files.size(path);
            if (this.fileType == FileType.DIRECTORY) {
                this.size = -1;
            }
            this.lastModified = LocalDateTime.ofInstant(Files.getLastModifiedTime(path).toInstant(), ZoneId.of("Europe/Moscow"));
        } catch (IOException e) {
            throw new RuntimeException("Unable to create file information from path. Check the program permissions and try again.");
        }
    }

    public static String getStringSize(long bytes) {
        if(bytes < 1000){
            return String.format ("%,d bytes", bytes);
        }else if (bytes < 1000 * Math.pow(2, 10)) {
            return String.format ("%,d KB", (long)(bytes / Math.pow(2, 10)));
        }else if (bytes < 1000 * Math.pow(2, 20) ) {
            return String.format ("%,d MB", (long)(bytes / Math.pow(2, 20)));
        }else if (bytes < 1000 * Math.pow(2, 30) ) {
            return String.format ("%,d GB", (long)(bytes / Math.pow(2, 30)));
        }else if (bytes < 1000 * Math.pow(2, 40) ) {
            return String.format ("%,d TB", (long)(bytes / Math.pow(2, 40)));
        }
        return "n/a";
    }
}
