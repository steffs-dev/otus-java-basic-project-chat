package ru.otus.basic.project;

import java.net.Socket;

/**
 * Класс клиента чата.
 * Устанавливает соединение с сервером и запускает сессию клиента.
 */
public class Client {
    private final int PORT = 8081;
    private final String HOST = "localhost";

    /**
     * Конструктор выводит сообщение о начале подключения к серверу.
     */
    Client() {
        System.out.println(ConsoleColors.WHITE_UNDERLINED + "Подключение к чату..." + ConsoleColors.RESET);
    }

    /**
     * Запускает клиента: создает сокет, инициализирует сессию и обрабатывает сообщения в рамках сессии.
     */
    public void startClient() {
        try (Socket socket = new Socket(HOST, PORT)) {
            ClientSession clientSession = new ClientSession(socket);
            clientSession.handleMessage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
