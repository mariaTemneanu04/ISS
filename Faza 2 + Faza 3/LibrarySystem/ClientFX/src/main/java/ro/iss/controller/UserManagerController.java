package ro.iss.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import ro.iss.controller.factory.UserCellFactory;
import ro.iss.domain.Reader;
import ro.iss.domain.validator.ValidationException;
import ro.iss.services.IService;
import ro.iss.services.MyException;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.stream.StreamSupport;

public class UserManagerController implements Initializable {
    private IService service;
    private final ObservableList<Reader> readersObs = FXCollections.observableArrayList();

    @FXML
    protected ListView<Reader> allReadersListView;
    @FXML
    protected TextField firstNameField;
    @FXML
    protected TextField surnameField;
    @FXML
    protected TextField cnpField;
    @FXML
    protected TextField addressField;
    @FXML
    protected TextField phoneField;
    @FXML
    protected TextField usernameField;
    @FXML
    protected TextField passwordField;

    public void setService(IService service) {
        this.service = service;
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        allReadersListView.setItems(readersObs);
        allReadersListView.setCellFactory(lv -> new UserCellFactory());

        allReadersListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populateFields(newSelection);
            }
        });
    }

    public void initModel() throws MyException {
        initReaders();
    }

    private void initReaders() throws MyException {
        readersObs.setAll(
                StreamSupport.stream(this.service.findAllReaders().spliterator(), false)
                        .toList()
        );
    }

    private void populateFields(Reader reader) {
        firstNameField.setText(reader.getFirstName());
        surnameField.setText(reader.getLastName());
        cnpField.setText(reader.getCNP());
        addressField.setText(reader.getAddress());
        phoneField.setText(reader.getPhoneNumber());

        usernameField.setText(reader.getUsername());
        passwordField.setText(reader.getPassword());
    }

    @FXML
    protected void clearTextFields() {
        firstNameField.setText("");
        surnameField.setText("");
        cnpField.setText("");
        addressField.setText("");
        phoneField.setText("");

        usernameField.setText("");
        passwordField.setText("");

        allReadersListView.getSelectionModel().clearSelection();
    }

    @FXML
    protected void handleSaveAction() {
        String firstName = firstNameField.getText();
        String lastName = surnameField.getText();
        String cnp = cnpField.getText();
        String address = addressField.getText();
        String phone = phoneField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();

        try {
            Reader r = new Reader(username, password, firstName, lastName, cnp, address, phone);
            service.addReader(r);
            initReaders();
            showAlert("New reader added with succes!", Alert.AlertType.INFORMATION);
        } catch (MyException | ValidationException e) {
            showAlert("Add failed! " + e, Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(type.name());
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
