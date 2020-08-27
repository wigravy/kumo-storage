package database.entity;


public class User {
    private long id;
    private String name;
    private String hash;

    public User() {
    }

    public User(long id, String name, String hash) {
        this.id = id;
        this.name = name;
        this.hash = hash;
    }
}
