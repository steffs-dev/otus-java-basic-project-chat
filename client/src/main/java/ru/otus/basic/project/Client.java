package ru.otus.basic.project;

import java.net.Socket;

public class Client {

    private final int PORT = 8081;
    private final String HOST = "localhost";

    Client() {
        System.out.println("Connecting to " + HOST + ":" + PORT);

    }
    public void startClient() {
        try (Socket socket = new Socket(HOST, PORT)) {
            ClientSession clientSession = new ClientSession(socket);
            clientSession.handleMessage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
