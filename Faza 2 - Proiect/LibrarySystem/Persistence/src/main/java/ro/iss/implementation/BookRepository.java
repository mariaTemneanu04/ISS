package ro.iss.implementation;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import ro.iss.IBookRepository;
import ro.iss.domain.Book;

import java.util.List;

public class BookRepository implements IBookRepository {

    protected SessionFactory sessionFactory;

    public BookRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void save(Book entity) {
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
    public void delete(Long id) {
        Transaction tr = null;

        try (Session session = sessionFactory.openSession()) {
            tr = session.beginTransaction();
            Book b = session.get(Book.class, id);

            if (b != null) {
                session.delete(b);
            }

            tr.commit();
        } catch (Exception e) {
            if (tr != null)
                tr.rollback();

            e.printStackTrace();
        }
    }

    @Override
    public void update(Book entity) {
        Transaction tr = null;

        try (Session session = sessionFactory.openSession()) {
            tr = session.beginTransaction();
            session.update(entity);
            tr.commit();
        } catch (Exception e) {
            if (tr != null)
                tr.rollback();

            e.printStackTrace();
        }
    }

    @Override
    public Book findOne(Long id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(Book.class, id);
        }
    }

    @Override
    public Iterable<Book> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("from Book", Book.class).list();
        }
    }

    @Override
    public List<Book> filterBooks(String title, String author) {
        try (Session session = sessionFactory.openSession()) {
            String hql = "from Book b where 1=1";

            if (title != null && !title.isEmpty()) {
                hql += " and lower(b.title) like :title";
            }

            if (author != null && !author.isEmpty()) {
                hql += " and lower(b.author) like :author";
            }

            var query = session.createQuery(hql, Book.class);
            if (title != null && !title.isEmpty()) {
                query.setParameter("title", "%" + title.toLowerCase() + "%");
            }

            if (author != null && !author.isEmpty()) {
                query.setParameter("author", "%" + author.toLowerCase() + "%");
            }

            return query.list();
        }
    }
}
