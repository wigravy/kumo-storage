import UI.Controllers.AuthorizationController;
import UI.Controllers.Controller;
import network.Network;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Socket;


public class Main extends Application {
    private static AuthorizationController authorizationController;
    private static  Controller controller;
    private static Network network;
    private static Scene scene;
    private static Socket socket;

    static void setRoot(FXMLLoader fxml) throws IOException {
        scene.setRoot(fxml.load());
    }

    @Override
    public void start(Stage stage) throws Exception {
        // Нужно для получения контроллера и возможности передать ему ссылку на Network
        FXMLLoader authLoader = new FXMLLoader(getClass().getResource("/fxml/Authorization.fxml"));
        FXMLLoader simpleLoader = new FXMLLoader(getClass().getResource("/fxml/simple.fxml"));

        Parent root = authLoader.load();
        scene = new Scene(root);

        authorizationController = authLoader.getController();
        authorizationController.setNetwork(network);
        authorizationController.setSocket(socket);

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
        network = new Network("localhost", 8500);
        network.connectToServer();
        socket = network.getSocket();
        launch(args);
    }
}
