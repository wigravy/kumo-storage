import UI.Controllers.AuthorizationController;
import Utils.Network;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Main extends Application {
    private static Network network;

    @Override
    public void start(Stage stage) throws Exception{
        FXMLLoader authLoader = new FXMLLoader(getClass().getResource("/fxml/Authorization.fxml"));
        Parent authRoot = authLoader.load();
        Scene authScene = new Scene(authRoot);
        AuthorizationController authorizationController = authLoader.getController();
        authorizationController.setNetwork(network);
        stage.setTitle("Kumo storage");
        stage.setScene(authScene);
        stage.setResizable(false);
        stage.show();

    }

    public static void main(String[] args) {
        network = new Network("localhost", 8500);
        network.connectToServer();
        launch(args);
    }
}
