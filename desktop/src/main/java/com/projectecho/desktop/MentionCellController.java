package com.projectecho.desktop;

import com.projectecho.core.Mention;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class MentionCellController {

    @FXML
    private VBox cellVBox;
    @FXML
    private Label sourceLabel;
    @FXML
    private Label dateLabel;
    @FXML
    private Hyperlink urlLink;
    @FXML
    private Label sentimentLabel;
    @FXML
    private TextArea contentTextArea;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public void setMention(Mention mention) {
        sourceLabel.setText(mention.getSource());
        
        LocalDateTime localDateTime = LocalDateTime.ofInstant(mention.getFoundAt(), ZoneId.systemDefault());
        dateLabel.setText(DATE_FORMATTER.format(localDateTime));
        
        contentTextArea.setText(mention.getContent());

        if (mention.getSentiment() != null) {
            sentimentLabel.setText(mention.getSentiment().name());
            switch (mention.getSentiment()) {
                case POSITIVE:
                    sentimentLabel.setTextFill(Color.GREEN);
                    break;
                case NEGATIVE:
                    sentimentLabel.setTextFill(Color.RED);
                    break;
                default:
                    sentimentLabel.setTextFill(Color.BLACK);
                    break;
            }
        } else {
            sentimentLabel.setText("N/A");
            sentimentLabel.setTextFill(Color.GRAY);
        }

        urlLink.setOnAction(event -> {
            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(new URI(mention.getUrl()));
                }
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        });
    }

    public VBox getRoot() {
        return cellVBox;
    }
}