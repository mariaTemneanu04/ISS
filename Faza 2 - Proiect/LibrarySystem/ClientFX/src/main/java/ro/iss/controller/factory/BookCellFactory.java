package ro.iss.controller.factory;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import ro.iss.domain.Book;
import ro.iss.domain.Status;

import java.util.function.Consumer;

public class BookCellFactory extends ListCell<Book> {

    private final boolean isReader;
    private final Consumer<Book> onBorrow;

    public BookCellFactory(boolean isReader, Consumer<Book> onBorrow) {
        this.isReader = isReader;
        this.onBorrow = onBorrow;
    }

    @Override
    protected void updateItem(Book book, boolean empty) {
        super.updateItem(book, empty);
        if (empty || book == null) {
            setText(null);
            setGraphic(null);
        } else {
            Label titleLabel = new Label("Title: " + book.getTitle());
            titleLabel.getStyleClass().add("fullname-style");

            Label authorLabel = new Label("Author: " + book.getAuthor());
            authorLabel.getStyleClass().add("borrowed-by-style");

            Label statusLabel = new Label(book.getStatus().toString());
            if (book.getStatus() == Status.BORROWED)
                statusLabel.getStyleClass().add("borrowed-style");
            else
                statusLabel.getStyleClass().add("available-style");

            statusLabel.setStyle("-fx-padding: 0 0 0 20px;");

            VBox textContainer = new VBox(titleLabel, authorLabel);
            textContainer.setSpacing(5);
            textContainer.setAlignment(Pos.CENTER_LEFT);

            HBox fullContainer = new HBox(textContainer, statusLabel);
            fullContainer.setSpacing(10);
            fullContainer.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(textContainer, Priority.ALWAYS);

            Node finalGraphic;

            if (isReader && book.getStatus() == Status.AVAILABLE) {
                Button borrowButton = new Button("Borrow");
                borrowButton.getStyleClass().add("button");
                borrowButton.setVisible(isSelected());

                this.selectedProperty().addListener((obs, oldSel, newSel) -> {
                    borrowButton.setVisible(newSel);
                });

                borrowButton.setOnAction(e -> {
                    System.out.println("Borrowed book: " + book.getTitle());
                    onBorrow.accept(book);
                });

                StackPane stackPane = new StackPane(fullContainer, borrowButton);
                StackPane.setAlignment(borrowButton, Pos.CENTER);
                finalGraphic = stackPane;
            } else {
                finalGraphic = fullContainer;
            }

            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            setGraphic(finalGraphic);
        }
    }

}
