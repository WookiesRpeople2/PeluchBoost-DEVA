package com.example.spring_frontend;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.example.spring_frontend.MessageApplication.StageReadyEvent;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


@Component
public class StageListener implements ApplicationListener<StageReadyEvent>{
  private final String applicationTitle;
  private final String fxmlFilePath;
  private final ApplicationContext ac;

  private static final String styleSheetPath = "styles.css";

  public StageListener(
      @Value("${spring.application.ui.title}") String applicationTitle,
      @Value("login-view.fxml") String fxmlFilePath,
      ApplicationContext ac
  ){
    this.applicationTitle = applicationTitle;
    this.fxmlFilePath = fxmlFilePath;
    this.ac = ac;
  }

  @Override
  public void onApplicationEvent(StageReadyEvent stageReadyEvent){
    try{
      Stage stage = stageReadyEvent.getStage();
      Parent root = loadFxml(ac, fxmlFilePath);
      Scene scene = new Scene(root, 500, 500);
      stage.setScene(scene);
      stage.setTitle(this.applicationTitle);
      stage.show();
    }catch(IOException e){
      throw new RuntimeException(e);
    }
  }

  public static Parent loadFxml(ApplicationContext ac, String path) throws IOException {
    FXMLLoader fxmlLoader = new FXMLLoader(StageListener.class.getResource(path));
    fxmlLoader.setControllerFactory(ac::getBean);
    Parent root = fxmlLoader.load();
    root.getStylesheets().add(Objects.requireNonNull(StageListener.class.getResource(styleSheetPath)).toExternalForm());
    return root;
  }
}
