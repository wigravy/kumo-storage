package com.wigravy.kumoStorage.server.database.entity;

import lombok.Data;
import org.hibernate.annotations.Table;

import javax.persistence.*;

@Entity
@Table(appliesTo = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private long id;
    @Column
    private String name;
    @Column
    private String password;
    @Column(name = "root_directory")
    private String rootDirectory;

    public User() {
    }

    public User(String name, String password) {
        this.name = name;
        this.password = password;
    }

    @Override
    public String toString() {
        return String.format("User(id%d): [name = '%s', password = '%s', root directory = '%s']", id, name, password, rootDirectory);
    }
}
