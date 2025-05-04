package ro.iss.services;

import ro.iss.domain.Book;
import ro.iss.domain.Loan;
import ro.iss.domain.Reader;
import ro.iss.domain.User;
import ro.iss.domain.validator.ValidationException;

import java.util.List;

public interface IService {
    String findUserType(String username, String password) throws MyException;

    User authenticate(User user, IObserver o) throws MyException;
    void logout (User u, IObserver employeeObs) throws MyException;

    Iterable<Book> findAllBooks() throws MyException;
    Iterable<Reader> findAllReaders() throws MyException;
    Iterable<Loan> findAllLoans() throws MyException;

    List<Reader> getReadersByName(String name) throws MyException;
    List<Loan> getLoansFiltered(Reader reader, String title) throws MyException;
    List<Book> getBooksFiltered(String title, String author) throws MyException;
    List<Loan> getLoansForReader(Reader reader) throws MyException;

    void addBook(Book b) throws MyException;
    void deleteBook(Book b) throws MyException;
    void updateBook(Book b) throws MyException;

    void addLoan(Loan l) throws MyException;
    void deleteLoan(Loan l) throws MyException;
    void updateLoan(Loan l) throws MyException;

    void addReader(Reader r) throws MyException, ValidationException;
}
