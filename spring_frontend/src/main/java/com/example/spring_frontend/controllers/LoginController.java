package com.example.spring_frontend.controllers;

import com.example.spring_frontend.StageListener;
import com.example.spring_frontend.models.User;
import com.example.spring_frontend.services.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

@Component
public class LoginController {
    @FXML
    private TextField usernameField;

    private static User user;

    private final ApplicationContext ac;
    private final UserService userService;

    public LoginController(ApplicationContext ac) {
        this.ac = ac;
        this.userService = new UserService();
    }

    public static User getUser(){
        return user;
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        if (!username.isEmpty()) {
            try {
                Parent chatView = StageListener.loadFxml(ac, "chat-view.fxml");
                user = userService.findOrCreateUser(new User.Builder().username(username).build()).get();
                Stage stage = (Stage) usernameField.getScene().getWindow();
                Scene scene = new Scene(chatView, 800, 800);
                stage.setScene(scene);
            } catch (IOException | ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
