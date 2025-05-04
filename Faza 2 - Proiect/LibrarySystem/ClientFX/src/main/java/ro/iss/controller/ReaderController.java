package ro.iss.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.iss.controller.factory.BookCellFactory;
import ro.iss.controller.factory.LoanCellFactory;
import ro.iss.domain.Book;
import ro.iss.domain.Loan;
import ro.iss.domain.Reader;
import ro.iss.services.IObserver;
import ro.iss.services.IService;
import ro.iss.services.MyException;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;

public class ReaderController implements IObserver, Initializable {
    protected IService server;
    protected Stage loginStage;
    protected Reader current;
    private static final Logger logger= LogManager.getLogger();

    private final ObservableList<Book> booksObs = FXCollections.observableArrayList();
    private final ObservableList<Loan> loansObs = FXCollections.observableArrayList();

    @FXML
    protected Label fullNameLabel;
    @FXML
    protected Label addressLabel;
    @FXML
    protected Label cnpLabel;
    @FXML
    protected Label phoneLabel;
    @FXML
    protected ListView<Book> bookListView;
    @FXML
    protected ListView<Loan> borrowedBooksListView;
    @FXML
    protected TextField searchTitleTextField;
    @FXML
    protected TextField searchAuthorTextField;

    public void setStage(Stage stage) {
        this.loginStage = stage;
    }

    public void setService(IService server) {
        this.server = server;
    }

    public void setCurrent(Reader current) {
        this.current = current;

        fullNameLabel.setText(current.getFirstName() + " " + current.getLastName());
        addressLabel.setText("Address: " + current.getAddress());
        cnpLabel.setText("CNP: " + current.getCNP());
        phoneLabel.setText("Phone Number: " + current.getPhoneNumber());
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        bookListView.setItems(booksObs);
        bookListView.setCellFactory(lv -> new BookCellFactory(true, this::handleBorrowAction));

        borrowedBooksListView.setItems(loansObs);
        borrowedBooksListView.setCellFactory(lv -> new LoanCellFactory(null, false));
    }

    public void initModel() throws MyException {
        initAllBooks();
        initLoans();
    }

    public void initAllBooks() throws MyException {
        List<Book> all = (List<Book>) this.server.findAllBooks();
        booksObs.setAll(all);
    }

    public void initLoans() throws MyException {
        List<Loan> all = (List<Loan>) this.server.getLoansForReader(current);
        loansObs.setAll(all);
    }

    public void logout() {
        try {
            server.logout(current, this);
            loginStage.show();

        } catch (MyException e) {
            logger.error("Error in logout method: {}", e.getMessage());
            showAlert("Logout failed! " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }


    @FXML
    protected void handleLogOut() {
        logout();
        Stage stage = (Stage) this.loginStage.getScene().getWindow();
        stage.close();
    }


    @FXML
    protected void handleRefresh() throws MyException {
        initAllBooks();
    }

    @FXML
    protected void clearAction() {
        searchTitleTextField.clear();
        searchAuthorTextField.clear();

        bookListView.getSelectionModel().clearSelection();
    }


    @FXML
    protected void handleSearchAction() {
        String title = searchTitleTextField.getText();
        String author = searchAuthorTextField.getText();

        try {
            List<Book> filtered = server.getBooksFiltered(title, author);
            booksObs.setAll(filtered);
        } catch (MyException e) {
            showAlert("Search failed! " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    protected void handleBorrowAction(Book book) {
        try {
            Loan loan = new Loan(LocalDateTime.now(), LocalDateTime.now().plusDays(14), this.current, book);
            server.addLoan(loan);

        } catch (MyException e) {
            showAlert("Borrow action failed! " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @Override
    public void showBooks() {
        Platform.runLater(() -> {
            try {
                initAllBooks();
                initLoans();
            } catch (MyException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(type.name());
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
