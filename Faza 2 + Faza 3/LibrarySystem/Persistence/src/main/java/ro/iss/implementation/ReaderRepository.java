package ro.iss.implementation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import ro.iss.IReaderRepository;
import ro.iss.domain.Librarian;
import ro.iss.domain.Reader;

import java.util.List;

public class ReaderRepository extends UserAbsRepository<Reader> implements IReaderRepository {

    public ReaderRepository(SessionFactory sessionFactory) {
        super(sessionFactory, Reader.class);
    }

    @Override
    public List<Reader> filterByName(String name) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                            "from Reader r where lower(concat(r.firstName, ' ', r.lastName)) like :name", Reader.class)
                    .setParameter("name", "%" + name.toLowerCase() + "%")
                    .list();
        }
    }

}
