package com.example.spring_frontend;

import java.io.IOException;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.stage.Stage;
import packages.ORM.config.DatabaseConfig;

public class MessageApplication extends Application {

    private ConfigurableApplicationContext springContext;

    static final class StageReadyEvent extends ApplicationEvent{
      public Stage getStage(){
        return Stage.class.cast(getSource());
      }
      public StageReadyEvent(Stage source){
        super(source);
      }
    }

    @Override
    public void init() throws Exception {
      DatabaseConfig config = new DatabaseConfig.Builder()
				.url("jdbc:mysql://localhost:3306/peluchBoost?allowPublicKeyRetrieval=true")
				.username("root")
				.password("password")
				.basePackage("com.example.spring_frontend.models")
				.build();
		  DatabaseConfig.initialize(config);
      ApplicationContextInitializer<GenericApplicationContext> initializer =
              ac -> {
                ac.registerBean(Application.class, ()->MessageApplication.this);
                ac.registerBean(Parameters.class, this::getParameters);
                ac.registerBean(HostServices.class, this::getHostServices);

              };

      springContext = new SpringApplicationBuilder()
        .sources(SpringApplication.class)
        .initializers(initializer)
        .run(getParameters().getRaw().toArray(new String[0]));
    }

    @Override
    public void start(Stage stage) throws IOException {
      springContext.publishEvent(new StageReadyEvent(stage));
    }

    @Override
    public void stop() throws Exception {
      springContext.close();
      Platform.exit();
    }

    public static void main(String[] args) {
      launch(args);
    }
}
