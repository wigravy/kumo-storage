package Utils.Messages;

import utils.FileInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FileListMessage extends AbstractMessage{
    private List<FileInfo> files = new ArrayList<>();

    public List updateFileList(Path path) throws IOException {
        files = Files.list(path)
                .map(FileInfo::new)
                .collect(Collectors.toList());
        return files;
    }
}
