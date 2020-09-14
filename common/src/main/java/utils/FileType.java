package utils;

public enum FileType {
    FILE("F"), DIRECTORY("D");
    private String type;

    public String getType() {
        return type;
    }

    FileType(String type) {
        this.type = type;
    }
}
