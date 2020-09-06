package database.entity;


import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "user")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "name")
    private String name;

    @Column(name = "password")
    private String password;

    @Column(name = "root_directory")
    private String rootDirectory;

    public User() {
    }

    public User(String name, String password, String rootDirectory) {
        this.name = name;
        this.password = password;
        this.rootDirectory = rootDirectory;
    }

    @Override
    public String toString() {
        return String.format("User(id%d): [name = '%s', password = '%s', root directory = '%s']", id, name, password, rootDirectory);
    }
}
