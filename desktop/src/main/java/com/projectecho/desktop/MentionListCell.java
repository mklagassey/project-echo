package com.projectecho.desktop;

import com.projectecho.core.Mention;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class MentionListCell extends ListCell<Mention> {

    private VBox graphic;
    private MentionCellController controller;

    @Override
    protected void updateItem(Mention item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setGraphic(null);
        } else {
            // Lazy load the FXML and controller only when first needed.
            if (graphic == null) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/projectecho/desktop/MentionCell.fxml"));
                    graphic = loader.load();
                    controller = loader.getController();
                } catch (IOException e) {
                    // If this fails, the app is in an unrecoverable state.
                    e.printStackTrace();
                    throw new RuntimeException("Failed to load MentionCell.fxml", e);
                }
            }

            // Populate the cell with data and set it as the graphic.
            controller.setMention(item);
            setGraphic(graphic);
        }
    }
}