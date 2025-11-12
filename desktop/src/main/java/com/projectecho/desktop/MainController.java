package com.projectecho.desktop;

import com.projectecho.core.Keyword;
import com.projectecho.core.Mention;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;
import java.util.function.Predicate;

public class MainController {

    @FXML
    private TextField keywordInput;
    @FXML
    private ListView<Keyword> keywordListView;
    @FXML
    private ListView<Mention> mentionListView;
    
    @FXML
    private Label statusLabel;
    @FXML
    private ChoiceBox<String> sourceFilterChoiceBox;
    @FXML
    private ChoiceBox<String> keywordFilterChoiceBox;
    @FXML
    private ChoiceBox<String> sentimentFilterChoiceBox;
    @FXML
    private TitledPane mentionsPane;

    private final ObservableList<Keyword> keywords = FXCollections.observableArrayList();
    private final ObservableList<Mention> allMentions = FXCollections.observableArrayList();
    private FilteredList<Mention> filteredMentions;

    private final SqliteKeywordDao keywordDao;
    private final SqliteMentionDao mentionDao;

    public MainController(SqliteKeywordDao keywordDao, SqliteMentionDao mentionDao) {
        this.keywordDao = keywordDao;
        this.mentionDao = mentionDao;
    }

    @FXML
    public void initialize() {
        keywordListView.setItems(keywords);
        filteredMentions = new FilteredList<>(allMentions);
        mentionListView.setItems(filteredMentions);

        setupCellFactories();
        setupFilters();
        setupMentionsPane();
    }
    
    public void loadInitialData() {
        keywords.setAll(keywordDao.findAll());
        allMentions.setAll(mentionDao.findAll());
        applyFilters();
    }

    private void setupCellFactories() {
        keywordListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Keyword item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getPhrase());
            }
        });

        mentionListView.setCellFactory(param -> new MentionListCell());
    }

    private void setupFilters() {
        sourceFilterChoiceBox.getItems().addAll("All Sources", "Hacker News", "Reddit r/saas");
        sourceFilterChoiceBox.setValue("All Sources");

        keywordFilterChoiceBox.getItems().add("All Keywords");
        keywordFilterChoiceBox.setValue("All Keywords");
        keywords.addListener((javafx.collections.ListChangeListener.Change<? extends Keyword> c) -> {
            updateKeywordFilterChoices();
        });
        
        sentimentFilterChoiceBox.getItems().addAll("All Sentiments", "POSITIVE", "NEUTRAL", "NEGATIVE");
        sentimentFilterChoiceBox.setValue("All Sentiments");

        sourceFilterChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> applyFilters());
        keywordFilterChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> applyFilters());
        sentimentFilterChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> applyFilters());
    }

    private void applyFilters() {
        String selectedSource = sourceFilterChoiceBox.getValue();
        String selectedKeyword = keywordFilterChoiceBox.getValue();
        String selectedSentiment = sentimentFilterChoiceBox.getValue();

        if (selectedSource == null || selectedKeyword == null || selectedSentiment == null) {
            return;
        }

        Predicate<Mention> sourcePredicate = mention ->
                "All Sources".equals(selectedSource) || selectedSource.equals(mention.getSource());

        Predicate<Mention> keywordPredicate = mention ->
                "All Keywords".equals(selectedKeyword) || 
                (mention.getContent() != null && mention.getContent().toLowerCase().contains(selectedKeyword.toLowerCase()));
                
        Predicate<Mention> sentimentPredicate = mention ->
                "All Sentiments".equals(selectedSentiment) ||
                (mention.getSentiment() != null && selectedSentiment.equals(mention.getSentiment().name()));

        filteredMentions.setPredicate(sourcePredicate.and(keywordPredicate).and(sentimentPredicate));
        updateMentionsPaneTitle();
    }
    
    private void updateKeywordFilterChoices() {
        String selected = keywordFilterChoiceBox.getValue();
        keywordFilterChoiceBox.getItems().setAll("All Keywords");
        keywords.forEach(kw -> keywordFilterChoiceBox.getItems().add(kw.getPhrase()));
        
        if (keywordFilterChoiceBox.getItems().contains(selected)) {
            keywordFilterChoiceBox.setValue(selected);
        } else {
            keywordFilterChoiceBox.setValue("All Keywords");
        }
    }

    private void setupMentionsPane() {
        filteredMentions.addListener((javafx.collections.ListChangeListener.Change<? extends Mention> c) -> {
            updateMentionsPaneTitle();
        });
        updateMentionsPaneTitle();
    }

    private void updateMentionsPaneTitle() {
        int size = filteredMentions.size();
        mentionsPane.setText("Mentions (" + size + ")");
        if (size > 100) {
            mentionsPane.setExpanded(false);
        } else {
            mentionsPane.setExpanded(true);
        }
    }

    @FXML
    private void addKeyword() {
        String phrase = keywordInput.getText();
        if (phrase != null && !phrase.isEmpty()) {
            Keyword newKeyword = new Keyword(phrase);
            keywordDao.save(newKeyword);
            keywordInput.clear();
            keywords.setAll(keywordDao.findAll());
        }
    }

    public void addMentions(List<Mention> newMentions) {
        Platform.runLater(() -> {
            allMentions.addAll(0, newMentions);
        });
    }

    public void updateStatus(String text) {
        Platform.runLater(() -> statusLabel.setText(text));
    }
}