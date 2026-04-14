package com.mes.view;

import atlantafx.base.theme.PrimerDark;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class StageManager {

    private final ApplicationContext applicationContext;
    private Stage primaryStage;
    private double xOffset = 0;
    private double yOffset = 0;

    public StageManager(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    private FXMLLoader getLoader(String fxmlPath) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        loader.setControllerFactory(applicationContext::getBean);
        return loader;
    }

    public void showLogin() {
        Platform.runLater(() -> {
            try {
                javafx.application.Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());

                FXMLLoader loader = getLoader("/fxml/login.fxml");
                Parent root = loader.load();

                Scene scene = new Scene(root, 500, 400);

                root.setOnMousePressed(event -> {
                    xOffset = event.getSceneX();
                    yOffset = event.getSceneY();
                });
                root.setOnMouseDragged(event -> {
                    primaryStage.setX(event.getScreenX() - xOffset);
                    primaryStage.setY(event.getScreenY() - yOffset);
                });

                primaryStage.setScene(scene);
                primaryStage.setTitle("MES System - Login");
                primaryStage.setResizable(false);
                primaryStage.setMaximized(false);
                primaryStage.setWidth(500);
                primaryStage.setHeight(400);
                primaryStage.centerOnScreen();
                primaryStage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void showMain() {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = getLoader("/fxml/main.fxml");
                Parent root = loader.load();

                Scene scene = new Scene(root, 1280, 800);

                root.setOnMousePressed(event -> {
                    xOffset = event.getSceneX();
                    yOffset = event.getSceneY();
                });
                root.setOnMouseDragged(event -> {
                    primaryStage.setX(event.getScreenX() - xOffset);
                    primaryStage.setY(event.getScreenY() - yOffset);
                });

                primaryStage.setScene(scene);
                primaryStage.setTitle("MES System");
                primaryStage.setResizable(true);
                primaryStage.setMaximized(true);
                primaryStage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void close() {
        primaryStage.close();
    }

    public void minimize() {
        primaryStage.setIconified(true);
    }

    public void maximize() {
        if (primaryStage.isMaximized()) {
            primaryStage.setMaximized(false);
        } else {
            javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
            primaryStage.setX(screenBounds.getMinX());
            primaryStage.setY(screenBounds.getMinY());
            primaryStage.setWidth(screenBounds.getWidth());
            primaryStage.setHeight(screenBounds.getHeight());
        }
    }
}
