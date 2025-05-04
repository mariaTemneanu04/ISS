package ro.iss;

import ro.iss.domain.Librarian;
import ro.iss.domain.Reader;

import java.util.List;

public interface IReaderRepository extends IRepository<Long, Reader> {
    Reader authenticate(String username, String password);
    List<Reader> filterByName(String name);
}
