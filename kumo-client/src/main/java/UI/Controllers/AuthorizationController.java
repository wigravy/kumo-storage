package UI.Controllers;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import javafx.application.Platform;
import network.AbstractMessage;
import network.FileMessage;
import network.Network;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import lombok.Setter;
import network.ServiceMessage;


import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;


public class AuthorizationController implements Initializable {
    @Setter
    private Network network;

    @Setter
    private ObjectDecoderInputStream is;

    @Setter
    private Socket socket;

    @FXML
    TextField loginField;

    @FXML
    PasswordField passwordField;

    private AbstractMessage abstractMessage;


    public void btnLoginOnAction(ActionEvent event) {
        if (loginField.getText().isEmpty() || passwordField.getText().isEmpty()) {
            showDialog("Authorization error", "First you need to enter your username and password!", Alert.AlertType.ERROR);
        } else {
            String username = replaceForbiddenSymbols(loginField.getText());
            String password = replaceForbiddenSymbols(passwordField.getText());
            network.sendServiceMessage("/authorize " + username + " " + password);
            Platform.runLater(() -> {
                while (socket.isConnected()) {
                    try {
                        if (is.available() > 0) {
                            abstractMessage = (AbstractMessage) is.readObject();
                            if (abstractMessage instanceof ServiceMessage) {
                                ServiceMessage message = (ServiceMessage) abstractMessage;
                                String msg = message.getMessage();
                                if (msg.equals("/authorize BAD")) {
                                    showDialog("Authorization error", "Wrong login or password, please try again!", Alert.AlertType.ERROR);
                                } else if (msg.equals("/authorize OK")) {
                                    //смена сцены
                                    return;
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {

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
