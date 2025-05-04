package ro.iss.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import ro.iss.controller.factory.LoanCellFactory;
import ro.iss.controller.factory.UserCellFactory;
import ro.iss.domain.Loan;
import ro.iss.domain.Reader;
import ro.iss.services.IService;
import ro.iss.services.MyException;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class BorrowReturnController implements Initializable {

    private IService service;
    private final ObservableList<Loan> loansObs = FXCollections.observableArrayList();
    private final ObservableList<Reader> readersObs = FXCollections.observableArrayList();

    @FXML
    protected ListView<Loan> loansListView;
    @FXML
    protected ListView<Reader> readerListView;
    @FXML
    protected TextField titleField;
    @FXML
    protected TextField authorField;
    @FXML
    protected HBox userHBox;
    @FXML
    protected TextField returnDateField;
    @FXML
    protected TextField searchReaderTextField;
    @FXML
    protected TextField searchLoansTextField;
    @FXML
    protected DatePicker returnDatePicker;

    private Reader selectedReader = null;
    private String searchTitle = "";


    public void setService(IService service) {
        this.service = service;
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loansListView.setItems(loansObs);
        loansListView.setCellFactory(lv -> new LoanCellFactory(this::handleReturnAction, true));

        loansListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                 populateFields(newValue);
            }
        });

        readerListView.setItems(readersObs);
        readerListView.setCellFactory(lv -> new UserCellFactory());

        readerListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            selectedReader = newValue;
            try {
                applyFilters();
            } catch (MyException e) {
                showAlert("Couldn't filter loans! " + e, Alert.AlertType.ERROR);
            }
        });
    }


    public void initModel() throws MyException {
        initLoans();
        initReaders();
    }


    public void initLoans() throws MyException {
        List<Loan> all = (List<Loan>) this.service.findAllLoans();
        loansObs.setAll(all);
    }


    private void initReaders() throws MyException {
        List<Reader> all = (List<Reader>) this.service.findAllReaders();
        readersObs.setAll(all);
    }


    private void applyFilters() throws MyException {
        List<Loan> filteredLoans = service.getLoansFiltered(selectedReader, searchTitle);
        loansObs.setAll(filteredLoans);
    }


    @FXML
    protected void handleSearchReader() {
        try {
            String searchText = searchReaderTextField.getText().toLowerCase();

            if (searchText.isEmpty()) {
                initReaders();
            } else {
                List<Reader> filtered = service.getReadersByName(searchText);
                readersObs.setAll(filtered);
            }

        } catch (MyException e) {
            showAlert("Search failed! " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }


    @FXML
    protected void handleSearchLoans() throws MyException {
        searchTitle = searchLoansTextField.getText();
        applyFilters();
    }

    @FXML
    protected void handleApplyChanges() {
        Loan selectedLoan = loansListView.getSelectionModel().getSelectedItem();
        LocalDate updatedReturnDate = returnDatePicker.getValue();

        if (selectedLoan == null) {
            showAlert("Please select a loan to update.", Alert.AlertType.WARNING);
            return;
        }

        if (updatedReturnDate == null) {
            showAlert("Please select a loan to update.", Alert.AlertType.WARNING);
            return;
        }

        try {
            selectedLoan.setDateOfReturn(updatedReturnDate.atStartOfDay());
            service.updateLoan(selectedLoan);

            applyFilters();
            showAlert("Loan updated successfully!", Alert.AlertType.INFORMATION);

        } catch (MyException e) {
            showAlert("Couldn't update loan! " + e.getMessage(), Alert.AlertType.ERROR);
        }

    }

    private void handleReturnAction(Loan loan) {
        System.out.println("Returning: " + loan);

        try {
            service.deleteLoan(loan);
            showAlert("Book returned successfully!", Alert.AlertType.INFORMATION);
            initLoans();
        } catch (MyException e) {
            showAlert("Couldn't return book! " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void populateFields(Loan loan) {
        titleField.setText(loan.getBook().getTitle());
        authorField.setText(loan.getBook().getAuthor());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy hh:mm");
        returnDateField.setText(formatter.format(loan.getDateOfReturn()));

        returnDatePicker.setValue(null);
        setupDatePicker(returnDatePicker, loan.getDateOfReturn().toLocalDate(), loan.getDateOfReturn().plusDays(14).toLocalDate());

        populateUserCells(loan.getReader());
    }


    private void populateUserCells(Reader reader) {
        // Clear userHBox înainte
        userHBox.getChildren().clear();

        // Image
        ImageView imageView = new ImageView(new Image(Objects.requireNonNull(getClass().getResource("/images/user.png")).toExternalForm()));
        imageView.setFitWidth(30);
        imageView.setFitHeight(30);

        // Full name label
        Label fullNameLabel = new Label(reader.getFirstName() + " " + reader.getLastName());
        fullNameLabel.getStyleClass().add("fullname-style");

        // Username label
        Label usernameLabel = new Label("Username: " + reader.getUsername());
        usernameLabel.getStyleClass().add("username-style");

        VBox textContainer = new VBox(fullNameLabel, usernameLabel);
        textContainer.setSpacing(3);

        userHBox.getChildren().addAll(imageView, textContainer);
    }


    @FXML
    protected void clearReadersSelection() throws MyException {
        searchReaderTextField.clear();
        selectedReader = null;
        initReaders();
        applyFilters();
    }


    @FXML
    protected void clearLoansSelection() throws MyException {
        titleField.setText("");
        authorField.setText("");
        userHBox.getChildren().clear();
        returnDateField.setText("");

        searchLoansTextField.clear();
        searchTitle = "";
        applyFilters();
    }


    private void setupDatePicker(DatePicker datePicker, LocalDate loanDate, LocalDate dueDate) {
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);

                if (date.isBefore(loanDate) || date.isAfter(dueDate)) {
                    setDisable(true);
                    setStyle("-fx-background-color: #ffc0cb;");
                }
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
