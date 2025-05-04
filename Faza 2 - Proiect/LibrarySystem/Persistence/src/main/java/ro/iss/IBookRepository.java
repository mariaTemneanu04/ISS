package ro.iss;

import ro.iss.domain.Book;
import ro.iss.domain.Reader;

import java.util.List;

public interface IBookRepository extends IRepository<Long, Book> {
    List<Book> filterBooks(String title, String author);
}
