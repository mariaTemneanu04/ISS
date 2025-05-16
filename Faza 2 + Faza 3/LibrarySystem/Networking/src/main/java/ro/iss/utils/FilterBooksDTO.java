package ro.iss.utils;

import java.io.Serializable;

public class FilterBooksDTO implements Serializable {
    private String title;
    private String author;

    public FilterBooksDTO(String title, String author) {
        this.title = title;
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }
}
