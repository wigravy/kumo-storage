package network;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import lombok.Getter;

import Utils.Messages.AbstractMessage;
import Utils.Messages.ServiceMessage;
import Utils.Messages.FileMessage;

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
    private static AbstractMessage abstractMessage;

    public Network(String host, int port) {
        this.HOST = host;
        this.PORT = port;
    }

    public void connectToServer() {
        Thread thread = new Thread(() -> {
            try (Socket socket = new Socket(HOST, PORT)) {
                this.socket = socket;
                os = new ObjectEncoderOutputStream(socket.getOutputStream());
                is = new ObjectDecoderInputStream(socket.getInputStream());
            } catch (IOException e) {
            }
        });
        thread.setDaemon(true);
        thread.start();

        Thread in = new Thread(() -> {
            while (true) {
                try {
                    if (is.available() > -1) {
                        abstractMessage = (AbstractMessage) is.readObject();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
        in.setDaemon(true);
        in.start();

    }

    public void sendServiceMessage(String message) {
        try {
            os.writeObject(ServiceMessage.builder()
                    .message(message)
                    .build());
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getMessage() throws Exception {
        if (abstractMessage instanceof ServiceMessage) {
            ServiceMessage serviceMessage = (ServiceMessage) abstractMessage;
            System.out.println(serviceMessage.getMessage());
            return serviceMessage.getMessage();
        } else {
            throw new Exception("Bad request. The return value is not service message.");
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
