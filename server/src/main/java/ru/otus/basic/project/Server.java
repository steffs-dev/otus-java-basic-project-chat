package ru.otus.basic.project;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class Server {
    private final int PORT = 8081;
    private final ExecutorService executorService;
    private Security security;
    private final Map<String, ClientHandler> clientHandlers;


    public Server() {
        clientHandlers = new ConcurrentHashMap<>();
        executorService = Executors.newCachedThreadPool(getThread());
        security = new Security();
        System.out.println("Server started!");
    }

    public void startServer() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(this, socket);
                System.out.println("Подключился пользователь. Порт: " + socket.getPort());
                executorService.submit(clientHandler);
            }
        }
    }

    private static ThreadFactory getThread() {
        return r -> {
            Thread thread = new Thread(r);
            thread.setName("Server-Thread-" + thread.threadId());
            thread.setDaemon(true);
            thread.setUncaughtExceptionHandler((t, e) ->
                    System.err.println("Ошибка в потоке " + t.getName() + " :" +
                            e.getMessage()));
            return thread;
        };
    }

    public boolean isRegistered(String nickname) {
        return security.isRegistered(nickname);
    }

    public boolean isAuthenticated(String nickname, String password) {
        return security.isAuthenticated(nickname, password);
    }

    public ClientHandler getClientHandler(String nickname) {
        return clientHandlers.get(nickname);
    }

    public Roles getRole(String nickname) {
        return security.getRole(nickname);
    }
    public void setRole(String nickname, Roles role) {
        security.setRole(nickname, role);
    }

    public void subscribe(String nickname, String password, ClientHandler clientHandler) {
        try {
            clientHandlers.put(nickname, clientHandler);
            security.addClientCredentials(nickname, password);
            broadcast("Подключен новый пользователь " + nickname, clientHandler);
        } catch (Exception e) {
            System.out.println("Ошибка при добавлении пользователя");
            e.printStackTrace();

        }
    }

    public void removeClientFromCash(String nickname) {
        clientHandlers.remove(nickname);
    }

    public void unsubscribe(String nickname) {
        try {
            removeClientFromCash(nickname);
            security.deleteAuthPair(nickname);
        }catch (Exception e) {
            System.out.println("Ошибка при удалении пользователя");
            e.printStackTrace();
        }
    }

    public void broadcast(String message, ClientHandler sender) {
        clientHandlers.values().forEach(client -> {
            if (client != sender && client.isActive()) {
                client.sendMessage(message);
            }
        });
    }
}

