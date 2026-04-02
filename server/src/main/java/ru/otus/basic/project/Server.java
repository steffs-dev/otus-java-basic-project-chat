package ru.otus.basic.project;

import ru.otus.basic.project.Entities.User;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Класс серверной части чата.
 * Принимает подключения, создает обработчики клиентов, управляет их списком,
 * рассылает сообщения и хранит учетные данные.
 * PORT - порт, который слушает сервер
 * executorService - обработчик потоков
 * security - экземпляр класса безопасности
 * clientHandlers - мапа для хранения пары ник пользователя - обработчик пользователя
 * для подключенных к чату пользователей
 * dbService - сервис для работы с БД
 */
public class Server {
    private final int PORT = 8081;
    private final ExecutorService executorService;
    private final Map<User, ClientHandler> clientHandlers;
    private final DBService dbService;

    /**
     * Создает сервер: инициализирует пул потоков, создает таблицу зарегистрированных
     * пользователей, добавляет первого администратора и мапу клиентов.
     */
    public Server() {
        clientHandlers = new ConcurrentHashMap<>();
        executorService = Executors.newCachedThreadPool(getThread());
        try {
            dbService = new DBService();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при подключении к БД. " + e);
        }
        try {
            dbService.insertFirstAdmin();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при добавлении администратора" + e);
        }

        System.out.println("Server started!");
    }

    /**
     * Запускает сервер: бесконечно принимает подключения пользователей, создает их обработчики
     * и передает их в пул потоков.
     */
    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(this, socket);
                executorService.submit(clientHandler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Создаёт фабрику потоков с именованием и обработчиком необработанных исключений.
     *
     * @return фабрика потоков
     */
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

    /**
     * Проверяет, зарегистрирован ли пользователь.
     *
     * @param nickname логин
     * @return true, если есть в БД
     */
    public boolean isRegistered(String nickname) {
        return dbService.findByNickname(nickname);
    }

    /**
     * Проверяет, находится ли пользователь в сети (есть ли обработчик).
     * В т.ч.для проверки, чтобы не было возможности повторно войти в чат под тем же логином
     *
     * @param nickname логин
     * @return true, если онлайн
     */
    public boolean isLoggedIn(String nickname) {
        return findUserByNickname(nickname).isPresent();
    }

    private Optional<User> findUserByNickname(String nickname) {
        return clientHandlers.keySet().stream()
                .filter(u -> u.getName().equals(nickname))
                .findFirst();
    }
    /**
     * Геттер для обработчика клиента.
     *
     * @param nickname логин
     * @return обработчик или null
     */
    public ClientHandler getClientHandler(String nickname) {
        return findUserByNickname(nickname)
                .map(clientHandlers::get)
                .orElse(null);
    }

    /**
     * Геттер для роли пользователя.
     *
     * @param nickname логин
     * @return роль
     */
    public Roles getRole(String nickname) {
        return dbService.getRoleByNickname(nickname);
    }

    /**
     * Устанавливает роль пользователя.
     *
     * @param nickname логин
     * @param role     новая роль
     */
    public void setRole(String nickname, Roles role) {
        if (dbService.updateRole(nickname, role) != 1) {
            System.out.println(ConsoleColors.RED + "Ошибка при обновлении роли пользователя " +
                    nickname + " на " + role.getRoleDescription() + " в БД" + ConsoleColors.RESET);
        return;
        }
        findUserByNickname(nickname).ifPresent(user -> user.setRole(role));
    }

    public Optional<User> saveUser(String nickname, String password) {
        return dbService.insert(nickname, password);
    }

    public Optional<User> checkCredentials(String nickname, String password) {
        return dbService.findByNicknameAndPassword(nickname, password);
    }

    /**
     * Добавляет нового пользователя в мапу зарегистрированных пользователей БД,
     * в список подключенных к чату пользователей (clientHandlers)
     * Исключена операция добавления для пользователя с
     *
     * @param user      пользователь
     * @param clientHandler обработчик
     */
    public void subscribe(User user, ClientHandler clientHandler) {
        try {
            clientHandlers.put(user, clientHandler);
            broadcast(ConsoleColors.BLUE_BOLD + "Подключен новый пользователь " +
                    user.getName() + ConsoleColors.RESET, clientHandler, MessageSettings.NOT_SEND_TO_PUBLISHER);
            System.out.println(ConsoleColors.BLUE_BOLD + "Подключен новый пользователь " +
                    user.getName() + ConsoleColors.RESET);
        } catch (Exception e) {
            System.out.println(ConsoleColors.RED + "Ошибка при добавлении пользователя"
                    + ConsoleColors.RESET);
            e.printStackTrace();
        }
    }

    /**
     * Удаляет клиента из списка подключенных к чату пользователей.
     *
     * @param nickname ник пользователя
     */
    public void removeClientFromCash(String nickname) {
        findUserByNickname(nickname).ifPresent(user -> {
            clientHandlers.remove(user);
            System.out.println(ConsoleColors.BLUE_BOLD + "Пользователь " + user.getName() +
                    " отключен" + ConsoleColors.RESET);
        });
    }

    /**
     * Полностью удаляет пользователя из системы (из списка подключенных и списка
     * зарегистрированных пользователей).
     *
     * @param nickname логин
     */
    public void unsubscribe(String nickname) {
        try {
            if (dbService.delete(nickname) != 1) {
                System.out.println(ConsoleColors.RED + "Ошибка при удалении пользователя " +
                        nickname + " из БД" + ConsoleColors.RESET);
                return;
            }

            System.out.println(ConsoleColors.BLUE_BOLD + "Пользователь " + nickname + " удален" + ConsoleColors.RESET);
        } catch (Exception e) {
            System.out.println(ConsoleColors.RED + "Ошибка при удалении пользователя" + ConsoleColors.RESET);
            e.printStackTrace();
        }
    }

    /**
     * Рассылает сообщение всем клиентам или всем, кроме отправителя.
     *
     * @param message         текст сообщения
     * @param sender          отправитель (может быть null)
     * @param recipientsScope режим рассылки (всем клиентам или всем, кроме отправителя)
     */
    public void broadcast(String message, ClientHandler sender, MessageSettings recipientsScope) {
        if (recipientsScope == MessageSettings.SENT_TO_ALL) {
            clientHandlers.values().forEach(client -> {
                client.sendMessage(message);
            });
        } else if (recipientsScope == MessageSettings.NOT_SEND_TO_PUBLISHER) {
            clientHandlers.values().forEach(client -> {
                if (client != sender) {
                    client.sendMessage(message);
                }
            });
        }
    }
}

