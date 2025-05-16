package ro.iss.implementation;

import jakarta.persistence.OrderBy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.iss.IBookRepository;
import ro.iss.ILibrarianRepository;
import ro.iss.ILoanRepository;
import ro.iss.IReaderRepository;
import ro.iss.domain.*;
import ro.iss.domain.validator.ReaderValidator;
import ro.iss.domain.validator.ValidationException;
import ro.iss.domain.validator.Validator;
import ro.iss.services.IObserver;
import ro.iss.services.IService;
import ro.iss.services.MyException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Service implements IService {

    private final IReaderRepository readerRepository;
    private final ILibrarianRepository libraryRepository;
    private final IBookRepository bookRepository;
    private final ILoanRepository loanRepository;

    private final Map<Long, IObserver> loggedUsers;

    private final Validator<Reader> validator = new ReaderValidator();

    private static final Logger logger = LogManager.getLogger();

    public Service(IReaderRepository readerRepository,
                   ILibrarianRepository libraryRepository,
                   IBookRepository bookRepository,
                   ILoanRepository loanRepository) {
        this.readerRepository = readerRepository;
        this.libraryRepository = libraryRepository;
        this.bookRepository = bookRepository;
        this.loanRepository = loanRepository;

        this.loggedUsers = new ConcurrentHashMap<>();
        logger.info("Service created");
    }

    @Override
    public synchronized String findUserType(String username, String password) throws MyException {
        System.out.println("Trying to find user type for: " + username);

        User user = libraryRepository.authenticate(username, password);
        if (user != null) {
            return "Librarian";
        }

        user = readerRepository.authenticate(username, password);
        if (user != null) {
            return "Reader";
        }

        throw new MyException("Authentication failed!");
    }


    @Override
    public synchronized User authenticate(User user, IObserver observer) throws MyException {
        User authenticatedUser = null;

        authenticatedUser = libraryRepository.authenticate(user.getUsername(), user.getPassword());
        if (authenticatedUser == null) {
            authenticatedUser = readerRepository.authenticate(user.getUsername(), user.getPassword());
        }

        if (authenticatedUser != null) {
            if (loggedUsers.get(authenticatedUser.getId()) != null)
                throw new MyException("This user is already logged in!");

            loggedUsers.put(authenticatedUser.getId(), observer);
            return authenticatedUser;
        } else {
            throw new MyException("Authentication failed!");
        }
    }

    @Override
    public void logout (User u, IObserver employeeObs) throws MyException {
        IObserver localClient = loggedUsers.remove(u.getId());
        if (localClient == null) {
            throw new MyException("User " + u.getUsername() + " logged out!");
        }
    }

    @Override
    public Iterable<Book> findAllBooks() throws MyException {
        return bookRepository.findAll();
    }

    @Override
    public Iterable<Reader> findAllReaders() throws MyException {
        return readerRepository.findAll();
    }

    @Override
    public List<Reader> getReadersByName(String name) throws MyException {
        return readerRepository.filterByName(name);
    }


    @Override
    public Iterable<Loan> findAllLoans() {
        return loanRepository.findAll();
    }


    @Override
    public List<Loan> getLoansFiltered(Reader reader, String title) throws MyException {
        return loanRepository.filterLoans(reader, title);
    }

    @Override
    public List<Book> getBooksFiltered(String title, String author) throws MyException {
        return bookRepository.filterBooks(title, author);
    }

    @Override
    public List<Loan> getLoansForReader(Reader reader) {
        return loanRepository.loansForReader(reader);
    }


    @Override
    public void addBook(Book b) {
        logger.info("Notifying clients about adding book: " + b);
        bookRepository.save(b);

        notifyObservers();
    }

    @Override
    public void addReader(Reader r) throws ValidationException {
        // validator.validate(r);
        readerRepository.save(r);
    }

    @Override
    public void addLoan(Loan l) throws MyException {
        logger.traceEntry("Adding loan: " + l);

        Book book = l.getBook();
        loanRepository.save(l);
        book.setStatus(Status.BORROWED);
        updateBook(book);
    }

    @Override
    public void deleteBook(Book b) throws MyException {
        logger.info("Notifying clients about deleting book: " + b);
        bookRepository.delete(b.getId());

        notifyObservers();
    }

    @Override
    public void deleteLoan(Loan l) throws MyException {
        logger.traceEntry("Deleting loan: " + l);

        Book book = l.getBook();
        loanRepository.delete(l.getId());
        book.setStatus(Status.AVAILABLE);
        updateBook(book);
    }

    @Override
    public void updateBook(Book b) throws MyException {
        logger.info("Notifying clients about updating book: " + b);
        bookRepository.update(b);

        notifyObservers();
    }

    @Override
    public void updateLoan(Loan l) throws MyException {
        loanRepository.update(l);
    }

    private void notifyObservers() {
        for (IObserver o : loggedUsers.values()) {
            logger.info("Sending book to update to: " + o);
            o.showBooks();
        }
    }

}
