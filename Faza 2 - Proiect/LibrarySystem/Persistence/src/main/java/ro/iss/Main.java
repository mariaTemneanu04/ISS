package ro.iss;

import ro.iss.domain.Loan;
import ro.iss.domain.Reader;
import ro.iss.domain.User;
import ro.iss.implementation.LoanRepository;
import ro.iss.implementation.ReaderRepository;
import ro.iss.utils.HibernateUtils;

public class Main {
    public static void main(String[] args) {
        ILoanRepository loanRepository = new LoanRepository(HibernateUtils.getSessionFactory());
        for (Loan l : loanRepository.findAll()) {
            System.out.println(l);
        }

    }
}