package com.wigravy.kumoStorage.server.main;


import com.wigravy.kumoStorage.server.database.HibernateUtil;
import com.wigravy.kumoStorage.server.database.entity.User;
import org.hibernate.Session;

import java.nio.file.Path;

public class Authorization {

    public static Path getUserPath(String username){
        try (Session session = HibernateUtil.getSession()) {
            User user = new User();
            user = session.get(User.class, username);
            session.getTransaction().commit();
            return Path.of(user.getRootDirectory());
        }
    }

    public static void createUser(String username, String password) {
        try (Session session = HibernateUtil.getSession()) {
            session.beginTransaction();
            User user = new User(username, password);
            session.save(user);
            session.getTransaction().commit();
        }
    }

    public static boolean getUserByLoginPassword(String username, String password) {
        try (Session session = HibernateUtil.getSession()) {
            User user = session.get(User.class, username);
            session.getTransaction().commit();
            return password.equals(user.getPassword());
        }
    }
}

