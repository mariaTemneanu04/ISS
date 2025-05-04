package ro.iss.controller.factory;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import ro.iss.domain.Loan;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.function.Consumer;

public class LoanCellFactory extends ListCell<Loan> {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
    private final Consumer<Loan> onReturnAction;
    private final boolean isAdmin;

    public LoanCellFactory(Consumer<Loan> onReturnAction, boolean isAdmin) {
        this.onReturnAction = onReturnAction;
        this.isAdmin = isAdmin;
    }

    @Override
    protected void updateItem(Loan loan, boolean empty) {
        super.updateItem(loan, empty);

        if (empty || loan == null) {
            setText(null);
            setGraphic(null);
        } else {
            String imagePath = "/images/loan.png";
            ImageView imageView = new ImageView(new Image(Objects.requireNonNull(getClass().getResource(imagePath)).toExternalForm()));
            imageView.setFitWidth(50);
            imageView.setFitHeight(50);

            Label titleLabel = new Label("Title: " + loan.getBook().getTitle());
            titleLabel.getStyleClass().add("fullname-style");

            Label borrowedByLabel = new Label("Borrowed By: " + loan.getReader().getFirstName() + " " + loan.getReader().getLastName());
            borrowedByLabel.getStyleClass().add("borrowed-by-style");

            Label borrowedDateLabel = new Label("Borrowed Date: " + formatter.format(loan.getDateOfLoan()));
            Label returnByLabel = new Label("Return By: " + formatter.format(loan.getDateOfReturn()));
            borrowedDateLabel.getStyleClass().add("date-style");
            returnByLabel.getStyleClass().add("date-style");

            VBox textContainer = new VBox(titleLabel, borrowedByLabel, borrowedDateLabel, returnByLabel);
            textContainer.setSpacing(5);
            textContainer.setAlignment(Pos.CENTER_LEFT);

            HBox fullContainer = new HBox(imageView, textContainer);
            fullContainer.setSpacing(12);
            fullContainer.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(textContainer, Priority.ALWAYS);

            LocalDate returnLocalDate = loan.getDateOfReturn().toLocalDate();
            LocalDate today = LocalDate.now();

            if (returnLocalDate.isBefore(today)) {
                String overdueImagePath = "/images/atentie.png";
                returnByLabel.getStyleClass().add("overdue-style");

                ImageView overdueImage = new ImageView(new Image(Objects.requireNonNull(getClass().getResource(overdueImagePath)).toExternalForm()));
                overdueImage.setFitWidth(30);
                overdueImage.setFitHeight(30);
                fullContainer.getChildren().add(overdueImage);
            }


            if (isAdmin) {
                Button returnButton = new Button("Return");
                returnButton.getStyleClass().add("button");
                returnButton.setVisible(isSelected());

                returnButton.setOnAction(e -> {
                    System.out.println("Returned book: " + loan.getBook().getTitle());
                    onReturnAction.accept(loan);
                });

                this.selectedProperty().addListener((obs, oldSel, newSel) -> {
                    returnButton.setVisible(newSel);
                });

                fullContainer.getChildren().add(returnButton);

            } else {
                long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(today, returnLocalDate);
                String daysText = daysLeft >= 0
                        ? "Days left: " + daysLeft
                        : "Overdue by: " + Math.abs(daysLeft) + " day(s)";
                Label daysLabel = new Label(daysText);
                daysLabel.getStyleClass().add("borrowed-style");
                daysLabel.setStyle("-fx-padding: 0 0 0 6px;");

                fullContainer.getChildren().add(daysLabel);
            }

            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            setGraphic(fullContainer);
        }
    }
}

