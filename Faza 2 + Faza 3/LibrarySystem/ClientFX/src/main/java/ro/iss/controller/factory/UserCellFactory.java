package ro.iss.controller.factory;

import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import ro.iss.domain.Reader;

import java.util.Objects;

public class UserCellFactory extends ListCell<Reader> {

    private final Label fullNameLabel = new Label();
    private final Label usernameLabel = new Label();

    private final ImageView imageView = new ImageView();

    private final VBox textContainer = new VBox(fullNameLabel, usernameLabel);
    private final HBox fullContainer = new HBox(imageView, textContainer);

    public UserCellFactory() {
        textContainer.setSpacing(5);
        textContainer.setAlignment(Pos.CENTER_LEFT);

        fullContainer.setSpacing(20);
        fullContainer.setAlignment(Pos.CENTER_LEFT);

        imageView.setFitWidth(50);
        imageView.setFitHeight(50);

        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }

    @Override
    protected void updateItem(Reader item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            String imagePath = "/images/user.png";
            imageView.setImage(new Image(Objects.requireNonNull(getClass().getResource(imagePath)).toExternalForm()));

            fullNameLabel.setText(item.getFirstName() + " " + item.getLastName());
            fullNameLabel.getStyleClass().add("fullname-style");

            usernameLabel.setText("Username: " + item.getUsername());
            usernameLabel.getStyleClass().add("username-style");

            setGraphic(fullContainer);

        }
    }
}
