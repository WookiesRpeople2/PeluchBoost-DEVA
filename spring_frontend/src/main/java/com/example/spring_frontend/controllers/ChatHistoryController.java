package com.example.spring_frontend.controllers;

import com.example.spring_frontend.StageListener;
import com.example.spring_frontend.models.Message;
import com.example.spring_frontend.models.User;
import com.example.spring_frontend.services.MessageService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Component
public class ChatHistoryController {
    @FXML
    private ScrollPane scrollPane;

    @FXML
    private VBox chatHistoryBox;


    private final ApplicationContext ac;
    private final MessageService messageService;

    public ChatHistoryController(ApplicationContext ac) {
        this.ac = ac;
        this.messageService = new MessageService();
    }

    @FXML
    private void initialize() {
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setPannable(true);
        scrollPane.setOnScroll(event -> {
            double deltaY = event.getDeltaY() * 0.02;
            scrollPane.setVvalue(scrollPane.getVvalue() - deltaY);
        });
        chatHistoryBox.heightProperty().addListener((obs, oldVal, newVal) ->
                scrollPane.setVvalue(1.0)
        );
        loadChatHistory();
    }


    @FXML
    private void handleBackToUserList() {
        try {
            Parent userListView = StageListener.loadFxml(ac, "userlist-view.fxml");
            Stage stage = (Stage) chatHistoryBox.getScene().getWindow();
            Scene scene = new Scene(userListView, 800, 600);
            stage.setScene(scene);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void loadChatHistory() {
        try {
            List<Message> messages = messageService.getMessagesByUser(UserListController.getSelectedUser().getId()).get();

            Platform.runLater(() -> {
                chatHistoryBox.getChildren().clear();

                if (messages.isEmpty()) {
                    Label noMessagesLabel = new Label("No messages found for this user");
                    noMessagesLabel.setStyle("-fx-font-style: italic; -fx-text-fill: gray;");
                    chatHistoryBox.getChildren().add(noMessagesLabel);
                    return;
                }

                messages.sort(Comparator.comparing(Message::getCreatedAt));
                Map<LocalDate, List<Message>> messagesByDate = groupMessagesByDate(messages);

                LocalDate previousDate = null;

                for (Map.Entry<LocalDate, List<Message>> entry : messagesByDate.entrySet()) {
                    LocalDate currentDate = entry.getKey();
                    List<Message> messagesForDate = entry.getValue();

                    if (previousDate == null || !previousDate.equals(currentDate)) {
                        addDateSeparator(currentDate);
                    }

                    for (Message message : messagesForDate) {
                        displayMessage(message);
                    }

                    previousDate = currentDate;
                }
            });
        } catch (InterruptedException | ExecutionException e) {
            Label errorLabel = new Label("Error loading chat history: " + e.getMessage());
            errorLabel.getStyleClass().add("error-label");
            Platform.runLater(() -> chatHistoryBox.getChildren().add(errorLabel));
        }
    }

    private Map<LocalDate, List<Message>> groupMessagesByDate(List<Message> messages) {
        Map<LocalDate, List<Message>> messagesByDate = new TreeMap<>();

        for (Message message : messages) {
            Date sqlDate = new Date(message.getCreatedAt().getTime());
            LocalDate localDate = sqlDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            if (!messagesByDate.containsKey(localDate)) {
                messagesByDate.put(localDate, new ArrayList<>());
            }

            messagesByDate.get(localDate).add(message);
        }

        return messagesByDate;
    }

    private void addDateSeparator(LocalDate date) {
        HBox separatorBox = new HBox();
        separatorBox.setAlignment(Pos.CENTER);
        separatorBox.setPadding(new Insets(10, 0, 10, 0));

        LocalDate today = LocalDate.now();
        String dateText;

        if (date.equals(today)) {
            dateText = "Today";
        } else if (date.equals(today.minusDays(1))) {
            dateText = "Yesterday";
        } else {
            Period period = Period.between(date, today);

            if (period.getYears() > 0) {
                dateText = period.getYears() + (period.getYears() == 1 ? " year ago" : " years ago");
            } else if (period.getMonths() > 0) {
                dateText = period.getMonths() + (period.getMonths() == 1 ? " month ago" : " months ago");
            } else {
                dateText = period.getDays() + (period.getDays() == 1 ? " day ago" : " days ago");
            }
        }

        Label dateLabel = new Label("------- " + dateText + " -------");
        dateLabel.setStyle("-fx-text-fill: gray;");

        separatorBox.getChildren().add(dateLabel);
        chatHistoryBox.getChildren().add(separatorBox);
    }

    private void displayMessage(Message message) {
        TextFlow messageFlow = new TextFlow();
        messageFlow.getStyleClass().add("message-container");
        messageFlow.setPadding(new Insets(5, 10, 5, 10));
        messageFlow.setMaxWidth(Double.MAX_VALUE);

        Text username = new Text(LoginController.getUser().getUsername() + ": ");
        username.getStyleClass().add("username");

        Text content = new Text(message.getContent() + "\n");
        content.getStyleClass().add("content");

        Text response = new Text("➥ " + message.getResponse() + "\n\n");
        response.getStyleClass().add("response");

        messageFlow.getChildren().addAll(username, content, response);

        chatHistoryBox.getChildren().add(messageFlow);
    }
}
