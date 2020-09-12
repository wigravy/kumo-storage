package controllers;


import Utils.Messages.AbstractMessage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.Setter;
import network.Network;

import java.io.IOException;


public class AuthorizationController {
    @Setter
    private Network network;

    @FXML
    TextField loginField;

    @FXML
    PasswordField passwordField;

    @FXML
    Button loginButton;



    public void btnLoginOnAction(ActionEvent event) {
        if (loginField.getText().isEmpty() || passwordField.getText().isEmpty()) {
            showDialog("Authorization error", "First you need to enter your username and password!", Alert.AlertType.ERROR);
        } else {
            String username = replaceForbiddenSymbols(loginField.getText());
            String password = replaceForbiddenSymbols(passwordField.getText());
            try {
                network.sendServiceMessage("/authorize " + username + " " + password);
                loginButton.setDisable(true);


            } catch (
                    IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void LoginError() {
        passwordField.clear();
        showDialog("Login Error", "Incorrect username or password", Alert.AlertType.ERROR);
        loginButton.setDisable(false);
    }


    @FXML
    public void initialize() {

    }


    public void showDialog(String title, String msg, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType, msg, ButtonType.OK);
        alert.setTitle(title);
        alert.showAndWait();
        loginButton.setDisable(false);
    }

    // Убираем все запрещенные символы
    private String replaceForbiddenSymbols(String str) {
        return str.replaceAll("[^0-9a-zA-Z&!?$#*]", "");
    }
}
