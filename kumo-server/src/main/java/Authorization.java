import database.entity.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class Authorization {
    private static final SessionFactory sessionFactory = new Configuration()
            .addAnnotatedClass(User.class)
            .buildSessionFactory();


    public boolean authorize(String username, String password) {
        Session session = null;
        session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        User user = session.get(User.class, username);
        session.getTransaction().commit();
        return user.getPassword().equals(password);
    }

    public boolean createUser(String username, String password) {
        Session session = null;
        session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        User user = new User(username, password, "/" + username);
        session.save(user);
        session.getTransaction().commit();
        return false;
    }

    public void close() {
        sessionFactory.close();
    }
}

