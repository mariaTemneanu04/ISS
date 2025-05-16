package ro.iss;

import ro.iss.domain.Librarian;

public interface ILibrarianRepository extends IRepository<Long, Librarian> {
    Librarian authenticate(String username, String password);
}
