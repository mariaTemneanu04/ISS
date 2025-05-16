package ro.iss.utils;

import ro.iss.domain.Reader;

import java.io.Serializable;

public class FilterLoansDTO implements Serializable {
    private Reader reader;
    private String title;

    public FilterLoansDTO(Reader reader, String title) {
        this.reader = reader;
        this.title = title;
    }

    public Reader getReader() {
        return reader;
    }

    public String getTitle() {
        return title;
    }
}
