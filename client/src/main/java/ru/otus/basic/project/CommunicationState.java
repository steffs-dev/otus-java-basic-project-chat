package ru.otus.basic.project;

/**
 * Класс состояния общения - основное состояние работы чата
 * (после успешной авторизации/регистрации).
 * Запускает отдельный поток для чтения входящих сообщений от сервера,
 * а в основном потоке читает команды пользователя из консоли.
 */
public class CommunicationState implements SessionState {

    /**
     * Запускает поток-читатель сообщений от сервера и основной цикл чтения команд пользователя.
     * При получении команд, влияющих на состояние (/exit, /login, /reg, /kick, /delete),
     * переключает состояние.
     * Цикл основного потока проверяет возможность чтения из консоли (метод ready()) с
     * периодичностью 100 мс (для обновления текущего состояния при его изменении командой,
     * полученной от сервера - внешнее управление сессией клиента)
     * @param clientSession текущая сессия клиента
     */
    @Override
    public void handleMessage(ClientSession clientSession) {
        Thread readerThread = new Thread(() -> {
            try {
                while (clientSession.isRunning()) {
                    String message = clientSession.getMessage();
                    if (message == null) {
                        break;
                    }
                    if (message.startsWith("/")) {
                        handleInputStateCommands(clientSession, parseCommand(message));
                    } else {
                        System.out.println(message);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        readerThread.start();

        while (clientSession.isRunning()) {
            try {
                while (clientSession.getConsoleReader().ready()) {
                    String message = clientSession.getConsoleReader().readLine();
                    clientSession.sendMessage(message);
                    if (message.startsWith("/")) {
                        handleOutputStateCommands(clientSession, parseCommand(message));
                    }
                }
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            readerThread.join();
        } catch (
                InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
    }

    /**
     * Извлекает команду из строки сообщения (первое слово до пробела), переводит в верхний
     * регистр для ставнимостью с командами (enum).
     *
     * @param message полное сообщение
     * @return команда в нижнем регистре
     */
    private String parseCommand(String message) {
        return message.split(" ")[0].toLowerCase();
    }

    /**
     * Обрабатывает команды, введенные пользователем, которые могут изменить состояние сессии.
     *
     * @param clientSession сессия
     * @param command       распознанная команда
     */
    private void handleOutputStateCommands(ClientSession clientSession, String command) {
        if (command.equals(Commands.EXIT.getCommand())) {
            clientSession.setState(new ExitState());
        } else if (command.equals(Commands.LOGIN.getCommand()) || command.equals(Commands.REGISTER.getCommand())) {
            clientSession.setState(new AuthorizationState());
        }
    }

    /**
     * Обрабатывает команды, полученные от сервера, которые могут изменить состояние сессии.
     *
     * @param clientSession сессия
     * @param command       распознанная команда
     */
    private void handleInputStateCommands(ClientSession clientSession, String command) {
        if (command.equals(Commands.EXIT.getCommand())) {
            clientSession.setState(new ExitState());
        } else if (command.equals(Commands.LOGIN.getCommand()) || command.equals(Commands.REGISTER.getCommand())) {
            clientSession.setState(new AuthorizationState());
        } else if (command.equals(Commands.LOGOUT.getCommand())) {
            System.out.println(ConsoleColors.BLUE + "Вашу сессию принудительно завершили" + ConsoleColors.RESET);
            clientSession.setState(new ExitState());
        } else if (command.equals(Commands.DELETE.getCommand())) {
            System.out.println(ConsoleColors.RED + "Вас удалили из чата" + ConsoleColors.RESET);
            clientSession.setState(new ExitState());
        }
    }
}
