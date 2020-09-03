package network;


import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import lombok.Getter;

import java.io.*;
import java.net.Socket;

public class Network {
    @Getter
    private Socket socket;
    private final String HOST;
    private final int PORT;
    private ObjectEncoderOutputStream os;
    @Getter
    private ObjectDecoderInputStream is;

    public Network(String host, int port) {
        this.HOST = host;
        this.PORT = port;
    }

    public void connectToServer() {
        Thread thread = new Thread(() -> {
            try {
                socket = new Socket(HOST, PORT);
                os = new ObjectEncoderOutputStream(socket.getOutputStream());
                is = new ObjectDecoderInputStream(socket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public void sendServiceMessage(String message) {
        try {
            os.writeObject(
                    ServiceMessage.builder()
                            .message(message)
                            .build());
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            is.close();
            os.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
