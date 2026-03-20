package ru.otus.basic.project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Обработчик клиента на стороне сервера.
 * Реализует Runnable, чтобы каждый обработчик выполнялся в отдельном потоке.
 * Отвечает за чтение сообщений от клиента, их обработку и отправку ответов.
 * reader - ресурс для чтения от клиента
 * writer - ресурс для отправки клиенту
 * server - ссылка на сервер
 * socket - сокет соединения сервера с клиентом
 * nickname - имя пользователя
 * running - рабочее состояние обработчика (true если работает)
 */
public class ClientHandler implements Runnable {
    private BufferedReader reader;
    private PrintWriter writer;
    private Server server;
    private Socket socket;
    private String nickname;
    private volatile boolean running;

    /**
     * Создаёт обработчик для конкретного клиента.
     *
     * @param server ссылка на сервер
     * @param socket сокет клиента
     * @throws IOException при ошибке создания потоков
     */
    public ClientHandler(Server server, Socket socket) throws IOException {
        this.server = server;
        this.socket = socket;
        reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
        writer = new PrintWriter(
                socket.getOutputStream(), true);
        nickname = String.valueOf(socket.getPort()); // временный ник до авторизации
        running = true;
    }

    /**
     * Проверка состояния обработчика - геттер для поля running
     *
     * @return running
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Сеттер для running
     *
     * @param running - состояние обработчика, которое необходимо присвоить параметру
     */
    public void setRunning(boolean running) {
        this.running = running;
    }

    /**
     * Основной цикл обработки сообщений от клиента.
     * Читает строки, если команда, то обрабатывает, иначе рассылает как обычное сообщение всем,
     * кроме отправителя.
     * При завершении удаляет пользователя из мапы сервера ник-обработчик, закрывает ресурсы
     */
    @Override
    public void run() {
        try {
            while (isRunning()) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                if (!running) {
                    break;
                }
                if (line.startsWith("/")) {
                    handleCommand(line);
                } else {
                    server.broadcast(ConsoleColors.PURPLE_BOLD + nickname + ": " +
                            ConsoleColors.WHITE_UNDERLINED + line + ConsoleColors.RESET, this, MessageSettings.NOT_SEND_TO_PUBLISHER);
                }
            }
        } catch (IOException e) {
            System.out.println(ConsoleColors.RED + "Пользователь: " + nickname + ". " + e.getMessage() + ConsoleColors.RESET);
        } finally {
            server.removeClientFromCash(nickname);
            disconnect();
        }
    }

    /**
     * Отправляет сообщение тому клиенту, чей это обработчик.
     * Описание команд в методу printCommands класса AuthorizationState
     *
     * @param message текст сообщения
     */
    public void sendMessage(String message) {
        writer.println(message);
    }

    /**
     * Разбирает и выполняет команду, полученную от клиента.
     *
     * @param command строка команды
     */
    public void handleCommand(String command) {
        String[] split = command.split(" ", 3);
        if (split.length == 3) {
            if (split[0].equals(Commands.REGISTER.getCommand())) {
                if (!server.isRegistered(split[1])) {
                    server.subscribe(split[1], split[2], this);
                    nickname = split[1];
                    sendMessage("loginCompleted");
                } else {
                    sendMessage("registrationFailed");
                }
            } else if (split[0].equals(Commands.LOGIN.getCommand())) {
                if (server.isLoggedIn(split[1])) {
                    sendMessage("authFailedAlreadyLoggedIn");
                    return;
                }
                if (server.isAuthenticated(split[1], split[2])) {
                    server.subscribe(split[1], split[2], this);
                    nickname = split[1];
                    sendMessage("loginCompleted");
                } else {
                    sendMessage("authFailed");
                }
            } else if (split[0].equals(Commands.WRITE_DIRECT.getCommand())) {
                ClientHandler clientHandler = server.getClientHandler(split[1]);
                if (server.isLoggedIn(split[1])) {
                    clientHandler.sendMessage(ConsoleColors.PURPLE_BOLD + nickname + ": " +
                            ConsoleColors.WHITE_UNDERLINED + split[2] + ConsoleColors.RESET);
                } else {
                    sendMessage(ConsoleColors.RED + "Не удалось отправить сообщение, т.к. сокет пользователя " +
                            split[1] + " закрыт" + ConsoleColors.RESET);
                }
            } else if (split[0].toLowerCase().equals
                    (Commands.GRANT_PRIVILEGES.getCommand())) {
                if (server.getRole(this.nickname).equals(Roles.ADMIN)) {
                    if (server.isRegistered(split[1])) {
                        try {
                            Roles role = Roles.valueOf(split[2].toUpperCase());
                            server.setRole(split[1], role);
                            server.getClientHandler(split[1]).sendMessage(ConsoleColors.BLUE +
                                    "Ваша роль изменена на " + ConsoleColors.BLUE_BOLD +
                                    role.getRoleDescription() + ConsoleColors.RESET);
                            sendMessage(ConsoleColors.BLUE + "Роль пользователя " + split[1] +
                                    " изменена на " + ConsoleColors.BLUE_BOLD +
                                    role.getRoleDescription() + ConsoleColors.RESET);
                        } catch (IllegalArgumentException e) {
                            sendMessage(ConsoleColors.RED + "Неизвестная роль " + split[2] + ConsoleColors.RESET);
                        }
                    } else {
                        sendMessage(ConsoleColors.RED + "Не удалось изменить роль пользователя " + split[1] +
                                ", т.к. пользователь не зарегистрирован" + ConsoleColors.RESET);
                    }
                } else {
                    sendMessage(ConsoleColors.RED + "У вас недостаточно прав для изменения роли пользователей" +
                            ConsoleColors.RESET);
                }
            }
        } else if (split.length == 2) {
            if (split[0].equals(Commands.LOGOUT.getCommand())) {
                if (server.getRole(this.nickname).equals(Roles.ADMIN)) {
                    ClientHandler clientHandler = server.getClientHandler(split[1]);
                    if (server.isLoggedIn(split[1])) {
                        clientHandler.setRunning(false);
                        clientHandler.sendMessage(Commands.LOGOUT.getCommand());
                        server.removeClientFromCash(split[1]);
                        server.broadcast(ConsoleColors.BLUE_BOLD + "Пользователь " +
                                split[1] + " отключен" + ConsoleColors.RESET, this, MessageSettings.SENT_TO_ALL);
                        clientHandler.disconnect();
                    } else {
                        sendMessage(ConsoleColors.RED + "Не удалось отключить пользователя " +
                                split[1] + ", т.к. сокет пользователя закрыт" + ConsoleColors.RESET);
                    }
                } else {
                    sendMessage(ConsoleColors.RED + "У вас недостаточно прав для отключения пользователей" + ConsoleColors.RESET);
                }
            } else if (split[0].equals(Commands.DELETE.getCommand())) {
                if (server.getRole(this.nickname).equals(Roles.ADMIN)) {
                    if (server.isRegistered(split[1])) {
                        if (server.isLoggedIn(split[1])) {
                            ClientHandler clientHandler = server.getClientHandler(split[1]);
                            clientHandler.sendMessage(Commands.LOGOUT.getCommand());
                            clientHandler.setRunning(false);
                            server.removeClientFromCash(split[1]);
                        }
                        server.unsubscribe(split[1]);
                        sendMessage(ConsoleColors.BLUE_BOLD + "Пользователь " + split[1] +
                                " удален" + ConsoleColors.RESET);
                    } else {
                        sendMessage(ConsoleColors.RED + "Не удалось удалить пользователя " + split[1] +
                                ", т.к. пользователь не зарегистрирован" + ConsoleColors.RESET);
                    }
                } else {
                    sendMessage(ConsoleColors.RED + "У вас недостаточно прав для отключения пользователей" + ConsoleColors.RESET);
                }
            }
        } else if (split.length == 1) {
            if (split[0].equals(Commands.EXIT.getCommand())) {
                sendMessage(Commands.EXIT.getCommand());
                server.broadcast(ConsoleColors.BLUE_BOLD + "Пользователь " + nickname +
                        " отключился" + ConsoleColors.RESET, this, MessageSettings.SENT_TO_ALL);
                server.removeClientFromCash(nickname);
            } else if (split[0].equals(Commands.MY_INFO.getCommand())) {
                sendMessage(ConsoleColors.BLUE + "Информация о пользователе:\n" +
                        "ник: " + ConsoleColors.BLUE_BOLD + nickname + "\n" +
                        ConsoleColors.BLUE + "роль: " + ConsoleColors.BLUE_BOLD +
                        server.getRole(nickname) + ConsoleColors.RESET);
            }
        } else {
            sendMessage(ConsoleColors.RED + "Неизвестная команда или неверное количество аргументов." + ConsoleColors.RESET);
            System.out.println(ConsoleColors.RED + "Неверная команда от " + nickname + ": " + command + ConsoleColors.RESET);
        }
    }

    /**
     * Закрывает все ресурсы обработчика.
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}