package controllers;


import Utils.Messages.AbstractMessage;
import Utils.Messages.ServiceMessage;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import lombok.Setter;
import main.Main;
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

    private AbstractMessage abstractMessage;


    public void btnLoginOnAction(ActionEvent event) {
        if (loginField.getText().isEmpty() || passwordField.getText().isEmpty()) {
            showDialog("Authorization error", "First you need to enter your username and password!", Alert.AlertType.ERROR);
        } else {
            String username = replaceForbiddenSymbols(loginField.getText());
            String password = replaceForbiddenSymbols(passwordField.getText());
            try {
                network.sendServiceMessage("/authorize " + username + " " + password);
                loginButton.setDisable(true);
                Thread thread = new Thread(authorizationTask);
                thread.setDaemon(true);
                thread.start();
                loginButton.setDisable(false);
            } catch (
                    IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Task<Void> authorizationTask = new Task<>() {
        int timer = 10;

        @Override
        protected Void call() throws Exception {
            do {
                abstractMessage = main.Main.getAbstractMessage();
                if (abstractMessage != null) {
                    if (abstractMessage instanceof ServiceMessage) {
                        ServiceMessage serviceMessage = (ServiceMessage) abstractMessage;
                        System.out.println("In controller: " + serviceMessage.getMessage());
                        String msg = serviceMessage.getMessage();
                        if (msg.equals("/authorize BAD")) {
                            Platform.runLater(() -> {
                                showDialog("Authorization error", "Wrong login or password, please try again!", Alert.AlertType.ERROR);
                            });
                            return null;
                        } else if (msg.equals("/authorize OK")) {
                            Platform.runLater(() -> {
                                try {
                                    Main.setRootSimple();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });
                            return null;
                        }
                    }
                } else {
                    Thread.sleep(200);
                    timer--;
                }
            } while (abstractMessage == null || timer != 0);
            return null;
        }
    };


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
