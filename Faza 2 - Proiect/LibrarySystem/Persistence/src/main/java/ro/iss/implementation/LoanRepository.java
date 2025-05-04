package ro.iss.implementation;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import ro.iss.ILoanRepository;
import ro.iss.domain.Loan;
import ro.iss.domain.Reader;

import java.util.List;

public class LoanRepository implements ILoanRepository {

    protected SessionFactory sessionFactory;

    public LoanRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void save(Loan entity) {
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
            Loan loan = session.get(Loan.class, id);

            if (loan != null) {
                session.delete(loan);
            }

            tr.commit();
        } catch (Exception e) {
            if (tr != null)
                tr.rollback();

            e.printStackTrace();
        }
    }

    @Override
    public void update(Loan entity) {
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
    public Loan findOne(Long id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(Loan.class, id);
        }
    }

    @Override
    public Iterable<Loan> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("from Loan", Loan.class).list();
        }
    }

    @Override
    public List<Loan> filterLoans(Reader reader, String title) {
        try (Session session = sessionFactory.openSession()) {
            String hql = "from Loan l where 1=1";

            if (reader != null) {
                hql += " and l.reader = :reader";
            }

            if (title != null && !title.isEmpty()) {
                hql += " and lower(l.book.title) like :title";
            }

            var query = session.createQuery(hql, Loan.class);

            if (reader != null) {
                query.setParameter("reader", reader);
            }

            if (title != null && !title.isEmpty()) {
                query.setParameter("title", "%" + title.toLowerCase() + "%");
            }

            return query.list();
        }
    }

    @Override
    public List<Loan> loansForReader(Reader reader) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                    "from Loan l where l.reader.id = :readerId", Loan.class)
                    .setParameter("readerId", reader.getId())
                    .list();
        }
    }


}
