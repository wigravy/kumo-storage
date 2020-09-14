package controllers;


import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import main.ClientApp;
import network.Network;

import java.io.IOException;


public class AuthorizationController {
    private Network network;
    private static AuthorizationController instance;

    public AuthorizationController() {
        instance = this;
    }

    public static AuthorizationController getInstance() {
        return instance;
    }

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
            String username = replaceInvalidSymbols(loginField.getText());
            String password = replaceInvalidSymbols(passwordField.getText());
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
    }

    public void btnShowHelp(ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Help");
        alert.setHeaderText("Hotkeys");
        alert.setContentText(
                "Ctrl + c : copy\n" +
                        "Ctrl + v : paste\n" +
                        "Enter : Enter to directory\n" +
                        "Delete : Delete file or directory\n" +
                        "Backspace: Enter to upper directory");
        alert.showAndWait();
    }

    private String replaceInvalidSymbols(String str) {
        return str.replaceAll("[^0-9a-zA-Z&!?$#*]", "");
    }

    public void btnExitOnAction(ActionEvent actionEvent) {
        network.close();
        Platform.exit();
    }
}
