package com.projectecho.desktop;

import com.projectecho.core.Keyword;
import com.projectecho.core.Mention;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
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
    @FXML
    private RadioMenuItem freq15m, freq30m, freq1h;

    private final ObservableList<Keyword> keywords = FXCollections.observableArrayList();
    private final ObservableList<Mention> allMentions = FXCollections.observableArrayList();
    private FilteredList<Mention> filteredMentions;

    private final SqliteKeywordDao keywordDao;
    private final SqliteMentionDao mentionDao;
    private BackgroundPollingService pollingService;

    public MainController(SqliteKeywordDao keywordDao, SqliteMentionDao mentionDao) {
        this.keywordDao = keywordDao;
        this.mentionDao = mentionDao;
    }

    public void setPollingService(BackgroundPollingService pollingService) {
        this.pollingService = pollingService;
    }

    @FXML
    public void initialize() {
        keywordListView.setItems(keywords);
        filteredMentions = new FilteredList<>(allMentions);
        mentionListView.setItems(filteredMentions);

        setupCellFactories();
        setupFilters();
        setupMentionsPane();
        setupPollingMenu();
    }
    
    public void loadInitialData() {
        keywords.setAll(keywordDao.findAll());
        allMentions.setAll(mentionDao.findAll());
        applyFilters();
    }

    private void setupPollingMenu() {
        ToggleGroup frequencyToggleGroup = new ToggleGroup();
        freq15m.setToggleGroup(frequencyToggleGroup);
        freq30m.setToggleGroup(frequencyToggleGroup);
        freq1h.setToggleGroup(frequencyToggleGroup);
        freq30m.setSelected(true); // Default

        frequencyToggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (pollingService == null) return;
            if (newToggle == freq15m) {
                pollingService.setPollingInterval(15);
            } else if (newToggle == freq30m) {
                pollingService.setPollingInterval(30);
            } else if (newToggle == freq1h) {
                pollingService.setPollingInterval(60);
            }
        });
    }
    
    @FXML
    private void checkNow() {
        if (pollingService != null) {
            pollingService.pollNow();
        }
    }

    private void setupCellFactories() {
        // **FIX:** Restore the correct cell factory for the keyword list
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
        
        keywords.addListener((ListChangeListener<Keyword>) c -> updateKeywordFilterChoices());
        
        sentimentFilterChoiceBox.getItems().addAll("All Sentiments", "POSITIVE", "NEUTRAL", "NEGATIVE");
        sentimentFilterChoiceBox.setValue("All Sentiments");
        
        // **FIX:** Use a ChangeListener to prevent the "one step behind" bug
        ChangeListener<String> filterListener = (obs, oldVal, newVal) -> applyFilters();
        sourceFilterChoiceBox.getSelectionModel().selectedItemProperty().addListener(filterListener);
        keywordFilterChoiceBox.getSelectionModel().selectedItemProperty().addListener(filterListener);
        sentimentFilterChoiceBox.getSelectionModel().selectedItemProperty().addListener(filterListener);
    }

    private void applyFilters() {
        String selectedSource = sourceFilterChoiceBox.getValue();
        String selectedKeyword = keywordFilterChoiceBox.getValue();
        String selectedSentiment = sentimentFilterChoiceBox.getValue();
        if (selectedSource == null || selectedKeyword == null || selectedSentiment == null) return;
        
        Predicate<Mention> sourcePredicate = mention -> "All Sources".equals(selectedSource) || selectedSource.equals(mention.getSource());
        Predicate<Mention> keywordPredicate = mention -> "All Keywords".equals(selectedKeyword) || (mention.getContent() != null && mention.getContent().toLowerCase().contains(selectedKeyword.toLowerCase()));
        Predicate<Mention> sentimentPredicate = mention -> "All Sentiments".equals(selectedSentiment) || (mention.getSentiment() != null && selectedSentiment.equals(mention.getSentiment().name()));
        
        filteredMentions.setPredicate(sourcePredicate.and(keywordPredicate).and(sentimentPredicate));
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
        filteredMentions.addListener((ListChangeListener<Mention>) c -> updateMentionsPaneTitle());
        updateMentionsPaneTitle();
    }

    private void updateMentionsPaneTitle() {
        int size = filteredMentions.size();
        mentionsPane.setText("Mentions (" + size + ")");
        mentionsPane.setExpanded(size <= 100);
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
        Platform.runLater(() -> allMentions.addAll(0, newMentions));
    }

    public void updateStatus(String text) {
        Platform.runLater(() -> statusLabel.setText(text));
    }
}