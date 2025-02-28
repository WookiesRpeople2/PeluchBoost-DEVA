package com.example.spring_frontend.controllers;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.example.spring_frontend.StageListener;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.example.spring_frontend.models.Message;
import com.example.spring_frontend.models.User;
import com.example.spring_frontend.services.MessageService;

import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

@Component
public class MessageController{
  @FXML private TextField messageField;
  @FXML private VBox chatBox;
  @FXML private ScrollPane scrollPane;

  private final MessageService messageService;
  private final ApplicationContext ac;

  public MessageController(ApplicationContext ac){
      this.ac = ac;
      this.messageService = new MessageService();
  }

  @FXML
  public void initialize(){
    scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    scrollPane.setPannable(true);
    scrollPane.setOnScroll(event -> {
          double deltaY = event.getDeltaY() * 0.02;
          scrollPane.setVvalue(scrollPane.getVvalue() - deltaY);
      });
      chatBox.heightProperty().addListener((obs, oldVal, newVal) ->
              scrollPane.setVvalue(1.0)
      );
  }

  @FXML
  private void handleSendMessage() throws ExecutionException, InterruptedException {
    String content = messageField.getText();
        
    if (LoginController.getUser().getUsername().isEmpty() || content.isEmpty()) {
        return;
    }
    
    String response = messageService.getQuote().get();
    Message message = new Message.Builder().content(content).response(response).user(LoginController.getUser()).build();
    messageService.saveMessage(message);

    displayMessage(message);
    messageField.clear();
  }


  private void displayMessage(Message message) {
    TextFlow messageFlow = new TextFlow();
    messageFlow.getStyleClass().add("message-container");

    Text username = new Text(LoginController.getUser().getUsername() + ": ");
    username.getStyleClass().add("username");

    Text content = new Text(message.getContent() + "\n");
    content.getStyleClass().add("content");

    Text response = new Text("➥ " + message.getResponse() + "\n\n");
    response.getStyleClass().add("response");

    messageFlow.getChildren().addAll(username, content, response);

    Platform.runLater(() -> chatBox.getChildren().add(messageFlow));
 }

    @FXML
    private void handleViewHistory() {
        try {
            Parent userListView = StageListener.loadFxml(ac, "userlist-view.fxml");
            Stage stage = (Stage) chatBox.getScene().getWindow();
            Scene scene = new Scene(userListView, 800, 600);
            stage.setScene(scene);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


//    private void loadChatHistory() {
//        try {
//            List<Message> messages = messageService.getMessagesByUser(user).get();
//
//            Platform.runLater(() -> {
//                chatBox.getChildren().clear();
//                messages.forEach(this::displayMessage);
//            });
//        } catch (InterruptedException | ExecutionException e) {
//            Label errorLabel = new Label("Error loading chat history: " + e.getMessage());
//            errorLabel.getStyleClass().add("error-label");
//            Platform.runLater(() -> chatBox.getChildren().add(errorLabel));
//        }
//    }

}
