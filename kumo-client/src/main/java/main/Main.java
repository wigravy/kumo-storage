package main;

import Utils.Messages.ServiceMessage;
import controllers.AuthorizationController;
import Utils.Messages.AbstractMessage;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import lombok.Getter;
import lombok.Setter;
import network.Callback;
import network.Network;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;


public class Main extends Application {
    private static Scene scene;
    private static Network network;
    private static AuthorizationController authorizationController;
    private static FXMLLoader authLoader = new FXMLLoader(Main.class.getResource("/fxml/Authorization.fxml"));
    private static FXMLLoader simpleLoader = new FXMLLoader(Main.class.getResource("/fxml/sample.fxml"));
    private static Stage stage;

    @Setter
    @Getter
    private static AbstractMessage abstractMessage;

    public static void setRootSimple() throws IOException {
        scene.setRoot(simpleLoader.load());
        stage.setResizable(true);
        stage.setHeight(900.0);
        stage.setWidth(1600.0);
        stage.centerOnScreen();
    }

    public static void setRootLogin() throws IOException {
        scene.setRoot(authLoader.load());
        stage.setResizable(false);
        stage.setHeight(700.0);
        stage.setWidth(500.0);
        stage.centerOnScreen();
    }


    @Override
    public void start(Stage stage) throws Exception {
        // Нужно для получения контроллера и возможности передать ему ссылку на Network


        Parent root = authLoader.load();
        scene = new Scene(root);

        authorizationController = authLoader.getController();
        authorizationController.setNetwork(network);
        this.stage = stage;
        stage.setTitle("Kumo storage");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> {
            network.close();
            Platform.exit();
        });
        stage.show();
    }



    public static void main(String[] args) {
        network = new Network("localhost", 8500, Main::setAbstractMessage);
        launch(args);
    }
}
