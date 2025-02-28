package com.example.spring_frontend.controllers;


import com.example.spring_frontend.StageListener;
import com.example.spring_frontend.models.User;
import com.example.spring_frontend.services.UserService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Component
public class UserListController {
    @FXML
    private ScrollPane scrollPane;

    @FXML
    private VBox usersBox;

    private final ApplicationContext ac;
    private final UserService userService;
    private static User selectedUser;

    public UserListController(ApplicationContext ac) {
        this.ac = ac;
        this.userService = new UserService();
    }

    @FXML
    private void initialize() {
        loadAllUsers();
    }

    @FXML
    private void handleBackToChat() {
        try {
            Parent chatView = StageListener.loadFxml(ac, "chat-view.fxml");
            Stage stage = (Stage) usersBox.getScene().getWindow();
            Scene scene = new Scene(chatView, 800, 800);
            stage.setScene(scene);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void loadAllUsers() {
        try {
            List<User> users = userService.getAllUsers().get();

            Platform.runLater(() -> {
                usersBox.getChildren().clear();

                for (User user : users) {
                    HBox userRow = createUserRow(user);
                    usersBox.getChildren().add(userRow);
                }
            });
        } catch (InterruptedException | ExecutionException e) {
            Label errorLabel = new Label("Error loading users: " + e.getMessage());
            errorLabel.getStyleClass().add("error-label");
            Platform.runLater(() -> usersBox.getChildren().add(errorLabel));
        }
    }

    private HBox createUserRow(User user) {
        HBox userRow = new HBox();
        userRow.setSpacing(10);
        userRow.setPadding(new Insets(5));
        userRow.getStyleClass().add("user-row");

        Label usernameLabel = new Label(user.getUsername());
        usernameLabel.getStyleClass().add("label");

        Button viewHistoryBtn = new Button("View History");
        viewHistoryBtn.setOnAction(event -> openChatHistory(user));

        userRow.getChildren().addAll(usernameLabel, viewHistoryBtn);
        return userRow;
    }

    private void openChatHistory(User user) {
        try {
            selectedUser = user;
            Parent root = StageListener.loadFxml(ac, "chathistory-view.fxml");
            Stage stage = (Stage) usersBox.getScene().getWindow();
            Scene scene = new Scene(root, 800, 600);
            stage.setScene(scene);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static User getSelectedUser() {
        return selectedUser;
    }
}
