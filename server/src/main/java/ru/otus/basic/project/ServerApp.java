package ru.otus.basic.project;

/**
 * Точка входа в серверное приложение.
 */
public class ServerApp {

    public static void main(String[] args) {
        Server server = new Server();
        server.startServer();
    }
}
