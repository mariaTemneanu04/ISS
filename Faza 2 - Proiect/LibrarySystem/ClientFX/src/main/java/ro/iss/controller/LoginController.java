package ro.iss.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.iss.StartClientFX;
import ro.iss.domain.Librarian;
import ro.iss.domain.Reader;
import ro.iss.domain.User;
import ro.iss.services.IService;
import ro.iss.services.MyException;

import java.io.IOException;
import java.util.Objects;

public class LoginController {
    @FXML
    private TextField username;

    @FXML
    private PasswordField password;

    @FXML
    private Text credentialsErrorText;

    private IService service;
    private ReaderController rdController;
    private LibrarianController lbrController;
    Parent librarianParent, readerParent;

    private static Logger logger = LogManager.getLogger(LoginController.class);

    public void setService(IService service) {
        this.service = service;
    }

    public void setParentForReader(Parent parent) {
        readerParent = parent;
    }

    public void setParentForLibrarian(Parent parent) {
        librarianParent = parent;
    }

    public void setReaderController(ReaderController rdController) {
        this.rdController = rdController;
    }

    public void setLibrarianController(LibrarianController lbrController) {
        this.lbrController = lbrController;
    }

    @FXML
    protected void onLogin(ActionEvent event) throws IOException, MyException {
        if (!username.getText().isEmpty() && !password.getText().isEmpty()) {
            try {
                User tempUser = new User(username.getText(), password.getText());
                String type = service.findUserType(tempUser.getUsername(), tempUser.getPassword());

                if (type.equals("Reader")) {

                    Reader reader = (Reader) service.authenticate(tempUser, rdController);
                    rdController.setCurrent(reader);

                    Stage loginStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    rdController.setStage(loginStage);
                    rdController.initModel();

                    Scene scene = new Scene(readerParent);
                    scene.getStylesheets().add(Objects.requireNonNull(StartClientFX.class.getResource("/css/login.css")).toExternalForm());
                    scene.getStylesheets().add(Objects.requireNonNull(StartClientFX.class.getResource("/css/text.css")).toExternalForm());
                    scene.getStylesheets().add(Objects.requireNonNull(StartClientFX.class.getResource("/css/button.css")).toExternalForm());
                    scene.getStylesheets().add(Objects.requireNonNull(StartClientFX.class.getResource("/css/listview.css")).toExternalForm());
                    loginStage.setScene(scene);

                    loginStage.setOnCloseRequest(event1 -> {
                        rdController.logout();
                        logger.debug("Closing application");
                        System.exit(0);
                    });

                    loginStage.show();

                } else if (type.equals("Librarian")) {
                    Librarian librarian = (Librarian) service.authenticate(tempUser, lbrController);

                    Stage loginStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    lbrController.setStage(loginStage);
                    lbrController.setCurrent(librarian);

                    Scene scene = new Scene(librarianParent);
                    scene.getStylesheets().add(Objects.requireNonNull(StartClientFX.class.getResource("/css/login.css")).toExternalForm());
                    scene.getStylesheets().add(Objects.requireNonNull(StartClientFX.class.getResource("/css/text.css")).toExternalForm());
                    scene.getStylesheets().add(Objects.requireNonNull(StartClientFX.class.getResource("/css/button.css")).toExternalForm());
                    scene.getStylesheets().add(Objects.requireNonNull(StartClientFX.class.getResource("/css/listview.css")).toExternalForm());
                    loginStage.setScene(scene);

                    loginStage.setOnCloseRequest(event1 -> {
                        lbrController.logout();
                        logger.debug("Closing application");
                        System.exit(0);
                    });

                    loginStage.show();

                } else {
                    throw new MyException("Something went wrong");
                }
            } catch (Exception e) {
                e.printStackTrace();
                credentialsErrorText.setVisible(true);
            }

            username.clear();
            password.clear();

        } else {
            System.out.println("GOL");
        }
    }

    public void onTextChanged(KeyEvent evt) {
        credentialsErrorText.setVisible(false);
    }

}
