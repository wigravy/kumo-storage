package com.wigravy.kumoStorage.client.controllers;


import com.wigravy.kumoStorage.client.main.ClientApp;
import com.wigravy.kumoStorage.common.utils.FileService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import com.wigravy.kumoStorage.client.network.Network;

import java.net.URL;
import java.util.ResourceBundle;


public class AuthorizationController implements Initializable {
    private Network network = Network.getInstance();
    @FXML
    TextField loginField;
    @FXML
    PasswordField passwordField;
    @FXML
    Button loginButton;
    FileService fileService = new FileService();


    public void btnLoginOnAction(ActionEvent event) {
        if (loginField.getText().isEmpty() || passwordField.getText().isEmpty()) {
            showDialog("Authorization error", "First you need to enter your username and password!", Alert.AlertType.ERROR);
        } else {
            String username = replaceInvalidSymbols(loginField.getText());
            String password = replaceInvalidSymbols(passwordField.getText());
            fileService.sendCommand(network.getChannel(), String.format("/authorization %s %s", username, password));
        }
    }

    public void loginError() {
        passwordField.clear();
        showDialog("Login Error", "Incorrect username or password", Alert.AlertType.ERROR);
        loginButton.setDisable(false);
    }


    public void showDialog(String title, String msg, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType, msg, ButtonType.OK);
        alert.setTitle(title);
        alert.showAndWait();
    }


    private String replaceInvalidSymbols(String str) {
        return str.replaceAll("[^0-9a-zA-Z&!?$#*]", "");
    }

    public void btnExitOnAction(ActionEvent actionEvent) {
        network.close();
        Platform.exit();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Thread t = new Thread(() -> {
            network.getMainHandler().setCallback(serviceMsg -> {
                System.out.println(serviceMsg);
                if (serviceMsg.equals("OK")) {
                    Platform.runLater(this::toMain);
                } else {
                    loginError();
                }
            });
        });
        t.start();
    }

    private void toMain() {
        ClientApp.getInstance().gotoMainApp();
    }
}
