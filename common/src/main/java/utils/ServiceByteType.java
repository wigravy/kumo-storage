package utils;

public enum  ServiceByteType {
    AUTH_OK(10),
    AUTH_ERR(11),

    UPLOAD_FILE (20),
    UPLOAD_FILE_ERR(21),

    DOWNLOAD_FILE (30),
    DOWNLOAD_FILE_ERR(31),

    DELETE_FILE (40),
    DELETE_FILE_ERR(41),

    SERVER_FILE_LIST(50),

    FILE_DOES_NOT_EXIST(80);



    final int value;

    ServiceByteType(int value) {
        this.value = value;
    }

    public byte getCommandBytes() {
        return (byte) value;
    }
}


