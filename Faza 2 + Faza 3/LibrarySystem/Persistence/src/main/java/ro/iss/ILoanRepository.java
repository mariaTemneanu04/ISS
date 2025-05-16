package ro.iss;

import ro.iss.domain.Loan;
import ro.iss.domain.Reader;

import java.util.List;

public interface ILoanRepository extends IRepository<Long, Loan> {
    List<Loan> filterLoans(Reader reader, String title);
    List<Loan> loansForReader(Reader reader);
}
