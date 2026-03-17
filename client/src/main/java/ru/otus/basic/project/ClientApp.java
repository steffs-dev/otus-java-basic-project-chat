package ru.otus.basic.project;

/**
 * Точка входа в клиентское приложение
 */
public class ClientApp {
    public static void main(String[] args) {
        Client client = new Client();
        client.startClient();
    }
}