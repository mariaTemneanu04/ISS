package ro.iss;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.iss.controller.LibrarianController;
import ro.iss.controller.LoginController;
import ro.iss.controller.ReaderController;
import ro.iss.protocol.ServiceRpcProxy;
import ro.iss.services.IService;

import java.io.File;
import java.util.Objects;
import java.util.Properties;


public class StartClientFX extends Application {
    private static int defaultPort = 55555;
    private static String defaultServer = "localhost";
    private static Logger logger = LogManager.getLogger(StartClientFX.class);


    @Override
    public void start(Stage stage) throws Exception {
        logger.info("Starting RPC client");
        Properties clientProp = new Properties();

        try {
            clientProp.load(StartClientFX.class.getResourceAsStream("/client.properties"));
            logger.info("Client properties set {} ", clientProp);

        } catch (Exception e) {
            logger.error("Cannot find client.properties " + e);
            logger.debug("Looking into folder {}", (new File(".")).getAbsolutePath());
            return;
        }

        String serverIP = clientProp.getProperty("librarysys.server.host", defaultServer);
        int serverPort = defaultPort;

        try {
            serverPort = Integer.parseInt(clientProp.getProperty("librarysys.server.port"));
        } catch (NumberFormatException ex) {
            logger.error("Wrong port number " + ex.getMessage());
            logger.debug("Using default port: " + defaultPort);
        }

        logger.info("Using server IP " + serverIP);
        logger.info("Using server port " + serverPort);

        IService server = new ServiceRpcProxy(serverIP, serverPort);

        FXMLLoader loader = new FXMLLoader(StartClientFX.class.getResource("/views/login-view.fxml"));
        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(Objects.requireNonNull(StartClientFX.class.getResource("/css/login.css")).toExternalForm());
        scene.getStylesheets().add(Objects.requireNonNull(StartClientFX.class.getResource("/css/text.css")).toExternalForm());
        scene.getStylesheets().add(Objects.requireNonNull(StartClientFX.class.getResource("/css/button.css")).toExternalForm());

        LoginController loginController = loader.getController();
        loginController.setService(server);

        FXMLLoader rdloader = new FXMLLoader(StartClientFX.class.getResource("/views/reader-view.fxml"));
        Parent rdroot = rdloader.load();
        ReaderController rdController = rdloader.getController();
        rdController.setService(server);
        loginController.setReaderController(rdController);
        loginController.setParentForReader(rdroot);

        FXMLLoader lbrloader = new FXMLLoader(StartClientFX.class.getResource("/views/librarian-view.fxml"));
        Parent lbrroot = lbrloader.load();
        LibrarianController lbrController = lbrloader.getController();
        lbrController.setService(server);
        loginController.setLibrarianController(lbrController);
        loginController.setParentForLibrarian(lbrroot);

        stage.setScene(scene);
        stage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}