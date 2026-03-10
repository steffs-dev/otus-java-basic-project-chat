package ru.otus.basic.project;

import java.io.IOException;

public class ServerApp {

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.startServer();
    }
}
