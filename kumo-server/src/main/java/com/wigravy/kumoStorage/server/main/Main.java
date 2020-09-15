package com.wigravy.kumoStorage.server.main;

public class Main {
    public static void main(String[] args) {
        Server server = new Server(8500);
        server.run();
    }
}
