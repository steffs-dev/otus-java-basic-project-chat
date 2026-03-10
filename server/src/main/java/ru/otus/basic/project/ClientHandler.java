package ru.otus.basic.project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private BufferedReader reader;
    private PrintWriter writer;
    private Server server;
    private Socket socket;
    private String nickname;
    private boolean isAuthenticated;

    public ClientHandler(Server server, Socket socket) throws IOException {
        this.server = server;
        this.socket = socket;
        reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
        writer = new PrintWriter(
                socket.getOutputStream(), true);
        nickname = String.valueOf(socket.getPort());
        isAuthenticated = false;
    }

    @Override
    public void run() {
        try {
            while (true) {
                String line = reader.readLine();
                if (line == null) { break;}
                if (line.startsWith("/")) {
                    handleCommand(line);
                } else {
                    server.broadcast(nickname + ": " + line, this);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            server.removeClientFromCash(nickname);
        }
    }

    public void sendMessage(String message) {
        writer.println(message);
    }

    public void handleCommand(String command) {
        String[] split = command.split(" ", 3);
        if (split.length == 3) {
            if (!isAuthenticated && split[0].equals(Commands.REGISTER.getCommand())) {
                if (!server.isRegistered(split[1])) {
                    server.subscribe(split[1], split[2], this);
                    nickname = split[1];
                    isAuthenticated = true;
                    server.broadcast("Пользователь: " + nickname +
                            " зарегистрировался", this);
                    sendMessage("loginCompleted");
                } else {
                    sendMessage("registrationFailed");
                }
            } else if (!isAuthenticated && split[0].equals(Commands.LOGIN.getCommand())) {
                if (server.isAuthenticated(split[1], split[2])) {
                    server.subscribe(split[1], split[2], this);
                    nickname = split[1];
                    isAuthenticated = true;
                    sendMessage("loginCompleted");
                } else {
                    sendMessage("authFailed");
                }
            } else if (isAuthenticated && split[0].equals(Commands.WRITE_DIRECT.getCommand())) {
                ClientHandler clientHandler = server.getClientHandler(split[1]);
                if (server.isRegistered(split[1]) && clientHandler.isActive()) {
                    clientHandler.sendMessage(nickname + ": " + split[2]);
                } else {
                    sendMessage("Не удалось отправить сообщение, т.к. " +
                            ((!server.isRegistered(split[1])) ?
                                    ("пользователь " + split[1] + " не зарегистрирован") :
                                    ("сокет пользователя " + split[1] + " закрыт")));
                }
            } else if (isAuthenticated && split[0].equals(Commands.GRANT_ADMIN_PRIVILEGES.getCommand())) {
                if (server.getRole(this.nickname).equals(Roles.ADMIN)) {
                    if (server.isRegistered(split[1])) {
                        try {
                            Roles role = Roles.valueOf(split[2]);
                            server.setRole(split[1], role);
                            server.getClientHandler(split[1]).sendMessage("Ваша роль изменена на " +
                                    role.getRoleDescription());
                            sendMessage("Роль пользователя " + split[1] + " изменена на " +
                                    role.getRoleDescription());
                        } catch (IllegalArgumentException e) {
                            sendMessage("Неизвестная роль " + split[2]);
                        }

                    } else {
                        sendMessage("Не удалось изменить роль пользователя " + split[1] +
                                ", т.к. пользователь не зарегистрирован");
                    }
                } else {
                    sendMessage("У вас недостаточно прав для изменения роли пользователей");
                }
            }
        } else if (isAuthenticated && split.length == 2) {
            if (split[0].equals(Commands.LOGOUT.getCommand())) {
                if (server.getRole(this.nickname).equals(Roles.ADMIN)) {
                    ClientHandler clientHandler = server.getClientHandler(split[1]);
                    if (server.isRegistered(split[1]) && clientHandler.isActive()) {
                        clientHandler.sendMessage(Commands.LOGOUT.getCommand());
                        sendMessage("Пользователь " + split[1] + " отключен");
                    } else {
                        sendMessage("Не удалось отключить пользователя " + split[1] + ", т.к. " +
                                ((!server.isRegistered(split[1])) ?
                                        ("пользователь не зарегистрирован") :
                                        ("сокет пользователя закрыт")));
                    }
                } else {
                    sendMessage("У вас недостаточно прав для отключения пользователей");
                }
            } else if (split[0].equals(Commands.DELETE.getCommand())) {
                if (server.getRole(this.nickname).equals(Roles.ADMIN)) {
                    ClientHandler clientHandler = server.getClientHandler(split[1]);
                    if (server.isRegistered(split[1])) {
                        if (clientHandler.isActive()) {
                            clientHandler.sendMessage(Commands.LOGOUT.getCommand());
                        }
                        server.unsubscribe(split[1]);
                        sendMessage("Пользователь " + split[1] + " удален");
                    } else {
                        sendMessage("Не удалось удалить пользователя " + split[1] +
                                ", т.к. пользователь не зарегистрирован");
                    }
                } else {
                    sendMessage("У вас недостаточно прав для отключения пользователей");
                }
            }
        } else if (split.length == 1 && split[0].equals(Commands.EXIT.getCommand())) {
            server.removeClientFromCash(nickname);
            sendMessage(Commands.EXIT.getCommand());
        } else {
            sendMessage("Неизвестная команда или неверное количество аргументов.");
            System.out.println("Неверная команда от " + nickname + ": " + command);
        }
    }

    public boolean isActive() {
        return !socket.isClosed();
    }
}