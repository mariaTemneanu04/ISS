package ro.iss.implementation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import ro.iss.ILibrarianRepository;
import ro.iss.domain.Librarian;

public class LibrarianRepository extends UserAbsRepository<Librarian> implements ILibrarianRepository {

    public LibrarianRepository(SessionFactory sessionFactory) {
        super(sessionFactory, Librarian.class);
    }

}
