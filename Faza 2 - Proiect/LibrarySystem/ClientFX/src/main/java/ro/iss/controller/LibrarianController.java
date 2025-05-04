package ro.iss.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.iss.StartClientFX;
import ro.iss.domain.Librarian;
import ro.iss.services.IObserver;
import ro.iss.services.IService;
import ro.iss.services.MyException;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class LibrarianController implements IObserver, Initializable {

    protected IService server;
    protected Stage loginStage;
    protected Librarian current;
    private static final Logger logger= LogManager.getLogger();

    private BookManagerController bookManagerController;
    private BorrowReturnController borrowReturnController;

    public void setStage(Stage stage) {
        this.loginStage = stage;
    }

    public void setService(IService server) {
        this.server = server;
    }

    public void setCurrent(Librarian current) {
        this.current = current;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) { }

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
    protected void handleUserManager() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/user-manager-view.fxml"));
            Scene scene = new Scene(loader.load());

            scene.getStylesheets().add(Objects.requireNonNull(StartClientFX.class.getResource("/css/login.css")).toExternalForm());
            scene.getStylesheets().add(Objects.requireNonNull(StartClientFX.class.getResource("/css/text.css")).toExternalForm());
            scene.getStylesheets().add(Objects.requireNonNull(StartClientFX.class.getResource("/css/button.css")).toExternalForm());
            scene.getStylesheets().add(Objects.requireNonNull(StartClientFX.class.getResource("/css/listview.css")).toExternalForm());

            UserManagerController controller = loader.getController();
            controller.setService(server);
            controller.initModel();

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("User Manager");
            stage.show();

        } catch (Exception e) {
            logger.error(e);
            showAlert("Couldn't open the window! " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    protected void handleBookCatalog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/books-manager-view.fxml"));
            Scene scene = new Scene(loader.load());

            scene.getStylesheets().add(Objects.requireNonNull(StartClientFX.class.getResource("/css/login.css")).toExternalForm());
            scene.getStylesheets().add(Objects.requireNonNull(StartClientFX.class.getResource("/css/text.css")).toExternalForm());
            scene.getStylesheets().add(Objects.requireNonNull(StartClientFX.class.getResource("/css/button.css")).toExternalForm());
            scene.getStylesheets().add(Objects.requireNonNull(StartClientFX.class.getResource("/css/listview.css")).toExternalForm());

            bookManagerController = loader.getController();
            bookManagerController.setService(server);
            bookManagerController.initModel();

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Book Catalog");

            stage.setOnCloseRequest(event -> bookManagerController = null);

            stage.show();

        } catch (Exception e) {
            logger.error(e);
            showAlert("Couldn't open the window! " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    protected void handleBorrowReturnManager() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/borrow-returns-view.fxml"));
            Scene scene = new Scene(loader.load());

            scene.getStylesheets().add(Objects.requireNonNull(StartClientFX.class.getResource("/css/login.css")).toExternalForm());
            scene.getStylesheets().add(Objects.requireNonNull(StartClientFX.class.getResource("/css/text.css")).toExternalForm());
            scene.getStylesheets().add(Objects.requireNonNull(StartClientFX.class.getResource("/css/button.css")).toExternalForm());
            scene.getStylesheets().add(Objects.requireNonNull(StartClientFX.class.getResource("/css/listview.css")).toExternalForm());
            scene.getStylesheets().add(Objects.requireNonNull(StartClientFX.class.getResource("/css/datepicker.css")).toExternalForm());

            borrowReturnController = loader.getController();
            borrowReturnController.setService(server);
            borrowReturnController.initModel();

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Borrow/Return Books");

            stage.setOnCloseRequest(event -> borrowReturnController = null);

            stage.show();

        } catch (Exception e) {
            logger.error(e);
            showAlert("Couldn't open the window! " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    protected void handleLogOut() {
        logout();
        Stage stage = (Stage) this.loginStage.getScene().getWindow();
        stage.close();
    }

    @Override
    public void showBooks() {
        Platform.runLater(() -> {
            if (bookManagerController != null) {
                System.out.println("Calling BookManagerController.showBooks()");
                try {
                    bookManagerController.showBookList();
                    borrowReturnController.initLoans();
                } catch (MyException e) {
                    throw new RuntimeException(e);
                }
            } else {
                System.out.println("BookManagerController is null (probably window not opened)");
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
