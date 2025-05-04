package ro.iss.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import ro.iss.controller.factory.BookCellFactory;
import ro.iss.domain.Book;
import ro.iss.domain.Status;
import ro.iss.services.IService;
import ro.iss.services.MyException;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class BookManagerController implements Initializable {

    private IService service;
    private final ObservableList<Book> booksObs = FXCollections.observableArrayList();

    @FXML
    protected ListView<Book> booksListView;
    @FXML
    protected TextField titleField;
    @FXML
    protected TextField authorField;

    public void setService(IService service) {
        this.service = service;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        booksListView.setItems(booksObs);
        booksListView.setCellFactory(lv -> new BookCellFactory(false, null));

        booksListView.getSelectionModel().selectedItemProperty().addListener((observable, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populateFields(newSelection);
            }
        });
    }

    public void initModel() throws MyException {
        showBookList();
    }

    public void showBookList() throws MyException {
        List<Book> all = (List<Book>) this.service.findAllBooks();
        booksObs.setAll(all);
    }

    private void populateFields(Book book) {
        titleField.setText(book.getTitle());
        authorField.setText(book.getAuthor());
    }

    @FXML
    protected void handleSaveAction() {
        String title = titleField.getText();
        String author = authorField.getText();

        if (title == null || author == null) {
            System.out.println("Title and author are null!");
            return;
        }

        try {
            Book b = new Book(title, author, Status.AVAILABLE);
            service.addBook(b);
            showAlert("You've added a new book!", Alert.AlertType.INFORMATION);

            clearTextFields();

        } catch (MyException e) {
            showAlert("Something went wrong!", Alert.AlertType.ERROR);
        }
    }

    @FXML
    protected void handleDeleteAction() {
        Book book = booksListView.getSelectionModel().getSelectedItem();

        if (book == null) {
            showAlert("Something went wrong!", Alert.AlertType.ERROR);
            return;
        }

        if (book.getStatus().equals(Status.BORROWED)) {
            showAlert(book.getTitle() + " is borrowed! You can't delete a borrowed book.", Alert.AlertType.INFORMATION);
        }

        try {
            service.deleteBook(book);
            showAlert("You've deleted a book!", Alert.AlertType.INFORMATION);

            clearTextFields();
        } catch (MyException e) {
            showAlert("Something went wrong!", Alert.AlertType.ERROR);
        }
    }

    @FXML
    protected void handleUpdateAction() {
        Book book = booksListView.getSelectionModel().getSelectedItem();

        if (book == null) {
            showAlert("Something went wrong!", Alert.AlertType.ERROR);
            return;
        }

        if (book.getStatus().equals(Status.BORROWED)) {
            showAlert(book.getTitle() + " is borrowed! You can't update a borrowed book.", Alert.AlertType.INFORMATION);
        }

        String newTitle = titleField.getText();
        String newAuthor = authorField.getText();

        if (newTitle == null || newAuthor == null) {
            showAlert("Title and author are null!", Alert.AlertType.ERROR);
            return;
        }

        if (newTitle.equals(book.getTitle()) && newAuthor.equals(book.getAuthor())) {
            showAlert(book.getTitle() + " is already in use!", Alert.AlertType.INFORMATION);
            return;
        }

        try {
            book.setTitle(newTitle);
            book.setAuthor(newAuthor);

            service.updateBook(book);
            showAlert("You've updated a book!", Alert.AlertType.INFORMATION);
            clearTextFields();

        } catch (MyException e) {
            showAlert("Something went wrong!", Alert.AlertType.ERROR);
        }
    }

    @FXML
    protected void clearTextFields() {
        titleField.setText("");
        authorField.setText("");

        booksListView.getSelectionModel().clearSelection();
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(type.name());
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
