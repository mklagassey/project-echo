package com.projectecho.desktop;

import com.projectecho.core.Keyword;
import com.projectecho.core.KeywordDao;
import com.projectecho.core.Mention;
import com.projectecho.core.MentionDao;
import com.projectecho.core.MentionListener;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MainController implements MentionListener {

    @FXML private BorderPane mentionsView, analyticsView;
    @FXML private Button mentionsButton, analyticsButton;
    @FXML private RadioMenuItem freq15m, freq30m, freq1h;
    @FXML private TextField keywordInput;
    @FXML private ListView<Keyword> keywordListView;
    @FXML private ListView<Mention> mentionListView;
    @FXML private Label statusLabel;
    @FXML private ChoiceBox<String> sourceFilterChoiceBox, keywordFilterChoiceBox, sentimentFilterChoiceBox;
    @FXML private ChoiceBox<String> mentionsDateFilterChoiceBox, analyticsTimeFilterChoiceBox, analyticsSourceFilterChoiceBox, analyticsKeywordFilterChoiceBox;
    @FXML private HBox mentionsCustomDateBox, analyticsCustomDateBox;
    @FXML private DatePicker mentionsStartDatePicker, mentionsEndDatePicker, analyticsStartDatePicker, analyticsEndDatePicker;
    @FXML private TitledPane mentionsPane;
    @FXML private Label totalMentionsLabel, sentimentRatioLabel, mostActiveSourceLabel;
    @FXML private PieChart sentimentPieChart;
    @FXML private BarChart<String, Number> topSourcesBarChart;
    @FXML private LineChart<String, Number> mentionsOverTimeChart;

    private final KeywordDao keywordDao;
    private final MentionDao mentionDao;
    private BackgroundPollingService pollingService;

    private final ObservableList<Keyword> keywords = FXCollections.observableArrayList();
    private final ObservableList<Mention> allMentions = FXCollections.observableArrayList();
    private FilteredList<Mention> filteredMentions;

    public MainController(KeywordDao keywordDao, MentionDao mentionDao) {
        this.keywordDao = keywordDao;
        this.mentionDao = mentionDao;
    }

    @FXML
    public void initialize() {
        keywordListView.setItems(keywords);
        filteredMentions = new FilteredList<>(allMentions);
        mentionListView.setItems(filteredMentions);
        mentionListView.setCellFactory(param -> new MentionListCell());
        keywordListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Keyword item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getPhrase());
            }
        });
        setupMentionsFilters();
        setupAnalyticsFilters();
        setupMentionsPane();
        setupPollingMenu();
        showMentionsView();
    }
    
    public void startServices(BackgroundPollingService pollingService) {
        this.pollingService = pollingService;
        this.pollingService.start();
        loadInitialData();
    }

    private void loadInitialData() {
        keywords.setAll(keywordDao.findAll());
        allMentions.setAll(mentionDao.findAll());
        applyFilters();
        refreshAnalytics();
    }

    @Override
    public void onMentionsFound(List<Mention> newMentions) {
        Platform.runLater(() -> {
            allMentions.addAll(0, newMentions);
            refreshAnalytics();
        });
    }

    @Override
    public void onStatusUpdate(String status) {
        Platform.runLater(() -> statusLabel.setText(status));
    }

    private void setupMentionsFilters() {
        sourceFilterChoiceBox.getItems().addAll("All Sources", "Hacker News", "Reddit r/saas");
        sourceFilterChoiceBox.setValue("All Sources");
        keywordFilterChoiceBox.getItems().add("All Keywords");
        keywordFilterChoiceBox.setValue("All Keywords");
        sentimentFilterChoiceBox.getItems().addAll("All Sentiments", "POSITIVE", "NEUTRAL", "NEGATIVE");
        sentimentFilterChoiceBox.setValue("All Sentiments");
        
        setupDateFilter(mentionsDateFilterChoiceBox, mentionsCustomDateBox, mentionsStartDatePicker, mentionsEndDatePicker, this::applyFilters);

        ChangeListener<Object> filterListener = (obs, oldVal, newVal) -> applyFilters();
        sourceFilterChoiceBox.getSelectionModel().selectedItemProperty().addListener(filterListener);
        keywordFilterChoiceBox.getSelectionModel().selectedItemProperty().addListener(filterListener);
        sentimentFilterChoiceBox.getSelectionModel().selectedItemProperty().addListener(filterListener);
    }

    private void setupAnalyticsFilters() {
        analyticsSourceFilterChoiceBox.getItems().addAll("All Sources", "Hacker News", "Reddit r/saas");
        analyticsSourceFilterChoiceBox.setValue("All Sources");
        analyticsKeywordFilterChoiceBox.getItems().add("All Keywords");
        analyticsKeywordFilterChoiceBox.setValue("All Keywords");
        
        keywords.addListener((ListChangeListener<Keyword>) c -> updateKeywordFilterChoices());
        
        setupDateFilter(analyticsTimeFilterChoiceBox, analyticsCustomDateBox, analyticsStartDatePicker, analyticsEndDatePicker, this::refreshAnalytics);
        
        analyticsSourceFilterChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> refreshAnalytics());
        analyticsKeywordFilterChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> refreshAnalytics());
    }

    private void setupDateFilter(ChoiceBox<String> choiceBox, HBox customDateBox, DatePicker startDatePicker, DatePicker endDatePicker, Runnable onFilterChange) {
        choiceBox.getItems().addAll("All Time", "Last 7 Days", "Last 30 Days", "Year to Date", "Custom Range...");
        choiceBox.setValue("All Time");
        choiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            customDateBox.setVisible("Custom Range...".equals(newVal));
            onFilterChange.run();
        });
        startDatePicker.valueProperty().addListener((obs, old, val) -> onFilterChange.run());
        endDatePicker.valueProperty().addListener((obs, old, val) -> onFilterChange.run());
    }

    private void applyFilters() {
        Predicate<Mention> sourcePredicate = mention -> "All Sources".equals(sourceFilterChoiceBox.getValue()) || sourceFilterChoiceBox.getValue().equals(mention.getSource());
        Predicate<Mention> keywordPredicate = mention -> "All Keywords".equals(keywordFilterChoiceBox.getValue()) || (mention.getContent() != null && mention.getContent().toLowerCase().contains(keywordFilterChoiceBox.getValue().toLowerCase()));
        Predicate<Mention> sentimentPredicate = mention -> "All Sentiments".equals(sentimentFilterChoiceBox.getValue()) || (mention.getSentiment() != null && sentimentFilterChoiceBox.getValue().equals(mention.getSentiment().name()));
        Predicate<Mention> datePredicate = getDatePredicate(mentionsDateFilterChoiceBox.getValue(), mentionsStartDatePicker.getValue(), mentionsEndDatePicker.getValue());
        
        filteredMentions.setPredicate(sourcePredicate.and(keywordPredicate).and(sentimentPredicate).and(datePredicate));
    }

    private Predicate<Mention> getDatePredicate(String timeRange, LocalDate startDate, LocalDate endDate) {
        Instant now = Instant.now();
        LocalDate today = now.atZone(ZoneId.systemDefault()).toLocalDate();
        
        Instant startInstant;
        Instant endInstant = now;

        if (timeRange == null) timeRange = "All Time";

        switch (timeRange) {
            case "Last 7 Days":
                startInstant = now.minus(7, ChronoUnit.DAYS);
                break;
            case "Last 30 Days":
                startInstant = now.minus(30, ChronoUnit.DAYS);
                break;
            case "Year to Date":
                startInstant = today.withDayOfYear(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
                break;
            case "Custom Range...":
                startInstant = (startDate != null) ? startDate.atStartOfDay(ZoneId.systemDefault()).toInstant() : Instant.MIN;
                endInstant = (endDate != null) ? endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant() : Instant.MAX;
                break;
            default: // "All Time"
                return mention -> true;
        }

        final Instant finalStart = startInstant;
        final Instant finalEnd = endInstant;
        return mention -> mention.getAuthoredAt() != null && mention.getAuthoredAt().isAfter(finalStart) && mention.getAuthoredAt().isBefore(finalEnd);
    }
    
    private void refreshAnalytics() {
        Predicate<Mention> datePredicate = getDatePredicate(analyticsTimeFilterChoiceBox.getValue(), analyticsStartDatePicker.getValue(), analyticsEndDatePicker.getValue());
        Predicate<Mention> sourcePredicate = mention -> "All Sources".equals(analyticsSourceFilterChoiceBox.getValue()) || analyticsSourceFilterChoiceBox.getValue().equals(mention.getSource());
        Predicate<Mention> keywordPredicate = mention -> "All Keywords".equals(analyticsKeywordFilterChoiceBox.getValue()) || (mention.getContent() != null && mention.getContent().toLowerCase().contains(analyticsKeywordFilterChoiceBox.getValue().toLowerCase()));

        List<Mention> filteredForAnalytics = allMentions.stream()
            .filter(datePredicate)
            .filter(sourcePredicate)
            .filter(keywordPredicate)
            .collect(Collectors.toList());
        
        updateKPIs(filteredForAnalytics);
        updateTopSourcesBarChart(filteredForAnalytics);
        updateMentionsOverTimeChart(filteredForAnalytics);
        
        Platform.runLater(() -> updateSentimentPieChart(filteredForAnalytics));
    }
    
    private void updateKeywordFilterChoices() {
        String mentionsSelected = keywordFilterChoiceBox.getValue();
        String analyticsSelected = analyticsKeywordFilterChoiceBox.getValue();
        
        keywordFilterChoiceBox.getItems().setAll("All Keywords");
        analyticsKeywordFilterChoiceBox.getItems().setAll("All Keywords");
        
        keywords.forEach(kw -> {
            keywordFilterChoiceBox.getItems().add(kw.getPhrase());
            analyticsKeywordFilterChoiceBox.getItems().add(kw.getPhrase());
        });

        keywordFilterChoiceBox.setValue(keywordFilterChoiceBox.getItems().contains(mentionsSelected) ? mentionsSelected : "All Keywords");
        analyticsKeywordFilterChoiceBox.setValue(analyticsKeywordFilterChoiceBox.getItems().contains(analyticsSelected) ? analyticsSelected : "All Keywords");
    }

    private void updateKPIs(List<Mention> mentions) {
        totalMentionsLabel.setText(String.valueOf(mentions.size()));
        long pos = mentions.stream().filter(m -> m.getSentiment() == Mention.Sentiment.POSITIVE).count();
        long neg = mentions.stream().filter(m -> m.getSentiment() == Mention.Sentiment.NEGATIVE).count();
        sentimentRatioLabel.setText(pos + ":" + neg);
        mostActiveSourceLabel.setText(mentions.stream().collect(Collectors.groupingBy(Mention::getSource, Collectors.counting())).entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("N/A"));
    }

    private void updateSentimentPieChart(List<Mention> mentions) {
        Map<Mention.Sentiment, Long> counts = mentions.stream().filter(m -> m.getSentiment() != null).collect(Collectors.groupingBy(Mention::getSentiment, Collectors.counting()));
        ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
        counts.forEach((sentiment, count) -> data.add(new PieChart.Data(sentiment.name(), count)));
        sentimentPieChart.setData(data);
    }

    private void updateTopSourcesBarChart(List<Mention> mentions) {
        Map<String, Long> counts = mentions.stream().collect(Collectors.groupingBy(Mention::getSource, Collectors.counting()));
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        counts.forEach((source, count) -> series.getData().add(new XYChart.Data<>(source, count)));
        topSourcesBarChart.getData().setAll(series);
    }

    private void updateMentionsOverTimeChart(List<Mention> mentions) {
        Map<LocalDate, Long> counts = mentions.stream().collect(Collectors.groupingBy(m -> m.getAuthoredAt().atZone(ZoneId.systemDefault()).toLocalDate(), Collectors.counting()));
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Mentions");
        counts.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(e -> series.getData().add(new XYChart.Data<>(e.getKey().toString(), e.getValue())));
        mentionsOverTimeChart.getData().setAll(series);
    }
    
    @FXML
    private void showMentionsView() {
        mentionsView.setVisible(true);
        analyticsView.setVisible(false);
        updateButtonStyles(mentionsButton);
    }

    @FXML
    private void showAnalyticsView() {
        refreshAnalytics();
        mentionsView.setVisible(false);
        analyticsView.setVisible(true);
        updateButtonStyles(analyticsButton);
    }

    private void updateButtonStyles(Button activeButton) {
        String activeStyle = "-fx-font-weight: bold; -fx-background-color: #cce5ff;";
        mentionsButton.setStyle(mentionsButton == activeButton ? activeStyle : "");
        analyticsButton.setStyle(analyticsButton == activeButton ? activeStyle : "");
    }

    private void setupPollingMenu() {
        ToggleGroup frequencyToggleGroup = new ToggleGroup();
        freq15m.setToggleGroup(frequencyToggleGroup);
        freq30m.setToggleGroup(frequencyToggleGroup);
        freq1h.setToggleGroup(frequencyToggleGroup);
        freq30m.setSelected(true);
        frequencyToggleGroup.selectedToggleProperty().addListener((obs, old, val) -> {
            if (pollingService == null) return;
            if (val == freq15m) pollingService.setPollingInterval(15);
            else if (val == freq30m) pollingService.setPollingInterval(30);
            else if (val == freq1h) pollingService.setPollingInterval(60);
        });
    }

    @FXML
    private void checkNow() {
        if (pollingService != null) pollingService.pollNow();
    }

    @FXML
    private void resetAllKeywords() {
        if (showConfirmation("Reset All Keywords?", "This will permanently delete all your keywords.")) {
            keywordDao.deleteAll();
            keywords.clear();
        }
    }

    @FXML
    private void resetAllMentions() {
        if (showConfirmation("Reset All Mentions?", "This will permanently delete all saved mentions.")) {
            mentionDao.deleteAll();
            allMentions.clear();
            refreshAnalytics();
        }
    }

    private void setupMentionsPane() {
        mentionsPane.setExpanded(true);
        filteredMentions.addListener((ListChangeListener<Mention>) c -> mentionsPane.setText("Mentions (" + filteredMentions.size() + ")"));
        mentionsPane.setText("Mentions (0)");
    }

    @FXML
    private void addKeyword() {
        String phrase = keywordInput.getText();
        if (phrase != null && !phrase.isEmpty()) {
            keywordDao.save(new Keyword(phrase));
            keywordInput.clear();
            keywords.setAll(keywordDao.findAll());
        }
    }

    @FXML
    private void deleteSelectedKeyword() {
        Keyword selected = keywordListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            keywordDao.delete(selected);
            keywords.setAll(keywordDao.findAll());
        }
    }

    private boolean showConfirmation(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}