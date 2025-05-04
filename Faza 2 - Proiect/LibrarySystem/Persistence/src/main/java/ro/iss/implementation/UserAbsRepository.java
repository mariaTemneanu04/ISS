package ro.iss.implementation;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import ro.iss.IRepository;
import ro.iss.domain.User;

public abstract class UserAbsRepository<T extends User> implements IRepository<Long, T> {

    protected final SessionFactory sessionFactory;
    private final Class<T> clazz;

    public UserAbsRepository(SessionFactory sessionFactory, Class<T> clazz) {
        this.sessionFactory = sessionFactory;
        this.clazz = clazz;
    }

    public void save(T entity) {
        Transaction tr = null;
        try (Session session = sessionFactory.openSession()) {
            tr = session.beginTransaction();
            session.persist(entity);
            tr.commit();
        } catch (Exception e) {
            if (tr != null) tr.rollback();
            e.printStackTrace();
        }
    }

    @Override
    public void delete(Long id) {}

    @Override
    public void update(T entity) {}



    public T findOne(Long id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(clazz, id);
        }
    }

    public Iterable<T> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("from " + clazz.getSimpleName(), clazz).list();
        }
    }

    public T authenticate(String username, String password) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                            "from " + clazz.getSimpleName() + " u where u.username = :username and u.password = :password", clazz)
                    .setParameter("username", username)
                    .setParameter("password", password)
                    .uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}
