package ru.otus.basic.project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Сессия клиента (контекст).
 * Содержит состояние, потоки ввода-вывода, ник.
 * Управляет переключением между состояниями и обработкой сообщений.
 * state - текущее состояние контекста. Переменная volatile, т.к. доступ к ней из нескольких мест
 * socket - сокет соединения с сервером
 * reader - ресурс для чтения от сервера
 * consoleReader - ресурс для чтения с консоли
 * writer - ресурс для отправки серверу
 * nickname - ник пользователя
 */
public class ClientSession {
    private volatile SessionState state;
    private Socket socket;
    private BufferedReader reader;
    private BufferedReader consoleReader;
    private PrintWriter writer;
    private String nickname;

    /**
     * Создает сессию клиента, инициализирует потоки и начальное состояние (AuthorizationState).
     *
     * @param socket сокет соединения с сервером
     */
    public ClientSession(Socket socket) {
        try {
            consoleReader = new BufferedReader(new InputStreamReader(System.in));
            state = new AuthorizationState();
            this.socket = socket;
            reader = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Геттер consoleReader
     *
     * @return consoleReader
     */
    public BufferedReader getConsoleReader() {
        return consoleReader;
    }

    /**
     * Сеттер для ника
     *
     * @param nickname - ник, присваиваемый параметру nickname
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * Сеттер для состояния
     *
     * @param state - состояние, присваиваемое параметру state
     */
    public void setState(SessionState state) {
        this.state = state;
    }

    /**
     * Геттер для состояния
     *
     * @return state
     */
    public SessionState getState() {
        return state;
    }

    /**
     * Основной цикл обработки: пока сессия активна (состояние не ExitState), делегирует обработку текущему состоянию.
     */
    public void handleMessage() {
        while (isRunning()) {
            state.handleMessage(this);
        }
        state.handleMessage(this);
    }

    /**
     * Проверяет, не находится ли сессия в состоянии выхода.
     *
     * @return true, если сессия еще активна (состояние не ExitState)
     */
    public boolean isRunning() {
        return !(this.state instanceof ExitState);
    }

    /**
     * Отправляет сообщение на сервер.
     *
     * @param message текст сообщения
     */
    public void sendMessage(String message) {
        writer.println(message);
    }

    /**
     * Получает сообщение от сервера.
     *
     * @return строка сообщения или команда выхода, если соединение разорвано
     * @throws IOException при ошибке чтения
     */
    public String getMessage() throws IOException {
        String message = reader.readLine();
        return (message == null) ? Commands.EXIT.getCommand() : message;
    }

    /**
     * Закрывает все ресурсы (потоки и сокет).
     */
    public void disconnect() {
        try {
            if (reader != null) {
                reader.close();
            }
            if (writer != null) {
                writer.close();
            }
            if (socket != null) {
                socket.close();
            }
            if (consoleReader != null) {
                consoleReader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
