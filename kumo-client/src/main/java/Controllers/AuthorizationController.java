package Controllers;

import javafx.application.Platform;
import lombok.Setter;
import network.Callback;
import network.Network;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import Utils.Messages.ServiceMessage;
import Utils.Messages.AbstractMessage;


import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;


public class AuthorizationController implements Initializable {
    @Setter
    private Network network;

    @Setter
    AbstractMessage abstractMessage;

    @FXML
    TextField loginField;

    @FXML
    PasswordField passwordField;


    public void btnLoginOnAction(ActionEvent event) {
        if (loginField.getText().isEmpty() || passwordField.getText().isEmpty()) {
            showDialog("Authorization error", "First you need to enter your username and password!", Alert.AlertType.ERROR);
        } else {
            String username = replaceForbiddenSymbols(loginField.getText());
            String password = replaceForbiddenSymbols(passwordField.getText());
            try {
                network.sendServiceMessage("/authorize " + username + " " + password);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Thread thread = new Thread(() -> {
            while (true) {
                if (abstractMessage != null) {
                    ServiceMessage serviceMessage = (ServiceMessage) abstractMessage;
                    System.out.println("In controller: " + serviceMessage.getMessage());
                    if (abstractMessage instanceof ServiceMessage) {


                        String msg = serviceMessage.getMessage();
                        if (msg.equals("/authorize BAD")) {
                            showDialog("Authorization error", "Wrong login or password, please try again!", Alert.AlertType.ERROR);
                        } else if (msg.equals("/authorize OK")) {
                            //смена сцены
                            return;
                        }
                    }
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }


    public void showDialog(String title, String msg, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType, msg, ButtonType.OK);
        alert.setTitle(title);
        alert.showAndWait();
    }

    // Убираем все запрещенные символы
    private String replaceForbiddenSymbols(String str) {
        return str.replaceAll("[^0-9a-zA-Z&!?$#*]", "");
    }


}
