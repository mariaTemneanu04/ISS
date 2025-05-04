package ro.iss.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@jakarta.persistence.Entity
@Table(name = "loans")
public class Loan extends Entity<Long> implements Serializable {

    @Column(name = "dateOfLoan")
    private LocalDateTime dateOfLoan;

    @Column(name = "dateOfReturn")
    private LocalDateTime dateOfReturn;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rid", nullable = false)
    private Reader reader;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bid", nullable = false)
    private Book book;

    public Loan() {}

    public Loan(LocalDateTime dateOfLoan, LocalDateTime dateOfReturn, Reader reader, Book book) {
        this.dateOfLoan = dateOfLoan;
        this.dateOfReturn = dateOfReturn;
        this.reader = reader;
        this.book = book;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getDateOfLoan() {
        return dateOfLoan;
    }

    public void setDateOfLoan(LocalDateTime dateOfLoan) {
        this.dateOfLoan = dateOfLoan;
    }

    public LocalDateTime getDateOfReturn() {
        return dateOfReturn;
    }

    public void setDateOfReturn(LocalDateTime dateOfReturn) {
        this.dateOfReturn = dateOfReturn;
    }

    public Reader getReader() {
        return reader;
    }

    public void setReader(Reader reader) {
        this.reader = reader;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    @Override
    public String toString() {
        return "Loan{" +
                "id=" + id +
                ", dateOfLoan=" + dateOfLoan +
                ", dateOfReturn=" + dateOfReturn +
                ", reader=" + reader +
                ", book=" + book +
                '}';
    }
}
