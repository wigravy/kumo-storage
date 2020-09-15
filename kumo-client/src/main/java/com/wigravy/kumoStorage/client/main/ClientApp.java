package com.wigravy.kumoStorage.client.main;


import com.wigravy.kumoStorage.client.network.Network;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.application.Application;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import lombok.Getter;

import java.io.IOException;

public class ClientApp extends Application {
    private Stage stage;
    private static ClientApp instance;
    @Getter
    private static Network network;

    public ClientApp() {
        instance = this;
    }

    public static ClientApp getInstance() {
        return instance;
    }



    public void gotoMainApp() {
        try {
            replaceSceneContent("/fxml/MainAppController.fxml");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void gotoLogin() {
        try {
            replaceSceneContent("/fxml/Authorization.fxml");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private Parent replaceSceneContent(String fxml) throws Exception {
        Parent page = FXMLLoader.load(ClientApp.class.getResource(fxml), null, new JavaFXBuilderFactory());
        Scene scene = stage.getScene();
        if (scene == null) {
            scene = new Scene(page);
            stage.setScene(scene);
        } else {
            stage.getScene().setRoot(page);
        }
        stage.sizeToScene();
        return page;
    }


    @Override
    public void start(Stage primaryStage) throws IOException {
        Scene scene = new Scene(new StackPane());
        stage = primaryStage;
        gotoLogin();
        primaryStage.show();
    }

    public static void main(String[] args) {
        network = new Network("localhost", 8500);
        launch();
    }
}
