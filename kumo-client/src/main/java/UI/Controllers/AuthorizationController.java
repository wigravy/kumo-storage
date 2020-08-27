package UI.Controllers;

import Utils.Network;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;


public class AuthorizationController implements Initializable {
    private Network network;
    @FXML
    TextField loginField;
    @FXML
    PasswordField passwordField;


    public void btnLoginOnAction(ActionEvent event) {
        if (loginField.getText().isEmpty() || passwordField.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "First you need to enter your username and password!", ButtonType.OK);
            alert.showAndWait();
        } else {
            String username = replaceForbiddenSymbols(loginField.getText());
            String password = replaceForbiddenSymbols(passwordField.getText());
            network.authorize(username, password);
        }
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    private String replaceForbiddenSymbols(String str) {
        return str.replaceAll("[^0-9a-zA-Z&!?$#*^]", "");
    }

    public void setNetwork(Network network) {
        this.network = network;
    }
}
