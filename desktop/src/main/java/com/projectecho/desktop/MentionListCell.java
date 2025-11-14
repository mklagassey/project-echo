package com.projectecho.desktop;

import com.projectecho.core.Mention;
import javafx.geometry.Insets;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class MentionListCell extends ListCell<Mention> {

    private final VBox layout = new VBox(5);
    private final HBox header = new HBox(10);
    private final Label sourceLabel = new Label();
    private final Label dateLabel = new Label();
    private final Hyperlink urlLink = new Hyperlink("Open Link");
    private final Label sentimentLabel = new Label();
    private final TextArea contentTextArea = new TextArea();

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public MentionListCell() {
        sourceLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        sourceLabel.setTextFill(Color.web("#007bff"));
        contentTextArea.setEditable(false);
        contentTextArea.setWrapText(true);
        contentTextArea.setPrefHeight(75);
        header.getChildren().addAll(sourceLabel, dateLabel, sentimentLabel, urlLink);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        layout.getChildren().addAll(header, contentTextArea);
        layout.setPadding(new Insets(10));
        layout.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5;");
    }

    @Override
    protected void updateItem(Mention item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setGraphic(null);
        } else {
            sourceLabel.setText(item.getSource());
            
            // Display the authoredAt date
            if (item.getAuthoredAt() != null) {
                LocalDateTime localDateTime = LocalDateTime.ofInstant(item.getAuthoredAt(), ZoneId.systemDefault());
                dateLabel.setText(DATE_FORMATTER.format(localDateTime));
            } else {
                dateLabel.setText("No Date");
            }
            
            contentTextArea.setText(item.getContent());

            if (item.getSentiment() != null) {
                sentimentLabel.setText(item.getSentiment().name());
                switch (item.getSentiment()) {
                    case POSITIVE: sentimentLabel.setTextFill(Color.GREEN); break;
                    case NEGATIVE: sentimentLabel.setTextFill(Color.RED); break;
                    default: sentimentLabel.setTextFill(Color.BLACK); break;
                }
            } else {
                sentimentLabel.setText("N/A");
                sentimentLabel.setTextFill(Color.GRAY);
            }

            urlLink.setOnAction(event -> {
                try {
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().browse(new URI(item.getUrl()));
                    }
                } catch (IOException | URISyntaxException e) {
                    e.printStackTrace();
                }
            });

            setGraphic(layout);
        }
    }
}