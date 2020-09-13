package main;


import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import lombok.Getter;
import lombok.Setter;
import network.Network;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class ClientApp extends Application {
    @Getter
    private static Network network;
    private Stage stage;
    private static ClientApp instance;
    private String authorizationFxml = "/fxml/Authorization.fxml";
    private String controllerFxml = "/fxml/sample.fxml";
    private String fileTableFxml = "/fxml/FileTablePane.fxml";
    private String serverTableFxml = "/fxml/ServerTable.fxml";

    public ClientApp() {
        instance = this;
    }

    public static ClientApp getInstance() {
        return instance;
    }

    @Setter
    private boolean isLogin = false;


    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        gotoLogin();
        stage.setOnCloseRequest(event -> {
            network.close();
            Platform.exit();
        });
        primaryStage.show();
    }

    private void gotoLogin() {
        try {
            replaceSceneContent(authorizationFxml);
            stage.setResizable(false);
            stage.setHeight(700.0);
            stage.setWidth(500.0);
            isLogin = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void gotoFileClient() {
        try {
            replaceSceneContent(controllerFxml);
            stage.setResizable(true);
            stage.setHeight(900.0);
            stage.setWidth(1600.0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void logout() {
        isLogin = false;
        changeScene();
    }


    public void changeScene() {
        Platform.runLater(() -> {
            if (isLogin) {
                gotoFileClient();
            } else {
                gotoLogin();
            }
        });
    }

    private Parent replaceSceneContent(String fxml) throws Exception {
        Parent page = FXMLLoader.load(ClientApp.class.getResource(fxml), null, new JavaFXBuilderFactory());
        Scene scene = stage.getScene();
        if (scene == null) {
            scene = new Scene(page, 500, 700);
            stage.setScene(scene);
        } else {
            stage.getScene().setRoot(page);
        }
        stage.sizeToScene();
        stage.centerOnScreen();
        return page;
    }


    public static void main(String[] args) {
        network = new Network("localhost", 8500);
        launch(args);
    }
}
