import Controllers.AuthorizationController;
import Utils.Messages.AbstractMessage;
import Utils.Messages.ServiceMessage;
import lombok.Getter;
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
        network = new Network("localhost", 8500, new Callback() {
            @Override
            public void call(AbstractMessage message) {
                ServiceMessage serviceMessage = (ServiceMessage) message;
                System.out.println("in main class: " + serviceMessage.getMessage());
                authorizationController.setAbstractMessage(message);
            }
        });
        launch(args);
    }
}
