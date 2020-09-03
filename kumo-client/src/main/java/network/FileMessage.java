package network;

public class FileMessage extends AbstractMessage{
    private String fileName;
    private byte [] fileData;

    private FileMessage(FileMessageBuilder builder) {
        fileName = builder.fileNameBuilder;
        fileData = builder.fileDataBuilder;
    }

    public String getName() {
        return fileName;
    }

    public byte[] getData() {
        return fileData;
    }

    public static FileMessageBuilder builder() {
        return new FileMessageBuilder();
    }

    public static class FileMessageBuilder {
        private String fileNameBuilder;
        private byte [] fileDataBuilder;

        public FileMessageBuilder name(String name) {
            fileNameBuilder = name;
            return this;
        }

        public FileMessageBuilder data(byte [] data) {
            fileDataBuilder = data;
            return this;
        }

        public FileMessage build() {
            return new FileMessage(this);
        }
    }
}
