package ru.otus.basic.project;

import java.io.IOException;

public class AuthorizationState implements SessionState {
    private String nickname;
    private String password;

    @Override
    public void handleMessage(ClientSession clientSession) {
        System.out.println("Доброе пожаловать в чат. Для регистрации введите " + Commands.REGISTER.getCommand() +
                ", для авторизации введите " + Commands.LOGIN.getCommand());
        while (true) {
            String command = clientSession.getScanner().nextLine().trim();
            if (command.equals(Commands.REGISTER.getCommand())) {
                if (processRegistration(clientSession)) {
                    clientSession.setState(new CommunicationState());
                    return;
                }
            } else if (command.equals(Commands.LOGIN.getCommand())) {
                if (processLogin(clientSession)) {
                    clientSession.setState(new CommunicationState());
                    return;
                }
            } else {
                System.out.println("Неверная команда. Введите" + Commands.REGISTER.getCommand() +
                        " или " + Commands.LOGIN.getCommand());
            }
        }
    }

    private void authorizationForm(ClientSession clientSession) {
        nickname = null;
        while (nickname == null || nickname.isBlank()) {
            System.out.println("Введите логин:");
            nickname = clientSession.getScanner().nextLine().trim();
            if (nickname.contains(" ")) {
                System.out.println("Логин не должен содержать пробелы.");
                nickname = null;
            }
        }

        password = null;
        while (password == null || password.isBlank()) {
            System.out.println("Введите пароль:");
            password = clientSession.getScanner().nextLine().trim();
            if (password.contains(" ")) {
                System.out.println("Пароль не должен содержать пробелы.");
                password = null;
            }
        }
        clientSession.setNickname(nickname);
        System.out.println("Вы ввели логин: " + nickname + ", пароль: " + password);

    }

    private void authorization(ClientSession clientSession) {
        authorizationForm(clientSession);
        clientSession.sendMessage(Commands.LOGIN.getCommand() + " " + nickname + " " + password);
    }

    private void registration(ClientSession clientSession) {
        authorizationForm(clientSession);
        clientSession.sendMessage(Commands.REGISTER.getCommand() + " " + nickname + " " + password);
    }

    private boolean processRegistration(ClientSession clientSession) {
        while (true) {
            registration(clientSession);

            try {
                String response = clientSession.getMessage();
                if (response == null || response.equals(Commands.EXIT.getCommand())) {
                    System.out.println("Соединение с сервером разорвано.");
                    clientSession.stopHandling();
                    return false;
                }

                if (response.equals("loginCompleted")) {
                    System.out.println("Добро пожаловать в чат, " + nickname);
                    printCommands();
                    return true;
                } else if (response.equals("registrationFailed")) {
                    System.out.printf("Пользователь с ником %s уже зарегистрирован. " +
                            "Выберите другой ник и повторите попытку.%n", nickname);
                } else {
                    System.out.println("Неизвестный ответ от сервера. Попробуйте снова.");
                }
            } catch (IOException e) {
                e.printStackTrace();
                clientSession.stopHandling();
                return false;
            }
        }
    }

    private boolean processLogin(ClientSession clientSession) {
        while (true) {
            authorization(clientSession);

            try {
                String response = clientSession.getMessage();
                if (response == null || response.equals(Commands.EXIT.getCommand())) {
                    System.out.println("Соединение с сервером разорвано.");
                    clientSession.stopHandling();
                    return false;
                }

                if (response.equals("loginCompleted")) {
                    System.out.println("Добро пожаловать в чат, " + nickname);
                    printCommands();
                    return true;
                } else if (response.equals("authFailed")) {
                    System.out.println("Неверный логин и/или пароль. Повторите попытку.");
                } else {
                    System.out.println("Неизвестный ответ от сервера. Попробуйте снова.");
                }
            } catch (IOException e) {
                e.printStackTrace();
                clientSession.stopHandling();
                return false;
            }
        }
    }

    private void printCommands() {
        String commandsList = "Для управления могут быть использованы следующие команды:\n" +
                "* " + Commands.REGISTER.getCommand() + " для регистрации, например, " +
                Commands.REGISTER.getCommand() + " nickname password\n" +
                "* " + Commands.LOGIN.getCommand() + " для входа в учетную запись, например, " +
                Commands.LOGIN.getCommand() + " nickname password\n" +
                "* " + Commands.WRITE_DIRECT.getCommand() + " для направления персонального сообщения, например, " +
                Commands.WRITE_DIRECT.getCommand() + " nickname(addressee) text\n" +
                "* " + Commands.EXIT.getCommand() + " для выхода из чата, например, " + Commands.EXIT.getCommand() + "\n" +
                "* " + Commands.LOGOUT.getCommand() + " для завершения сессии пользователя (доступна только администраторам)" +
                ", например, " + Commands.LOGOUT.getCommand() + " nickname\n" +
                "* " + Commands.GRANT_ADMIN_PRIVILEGES.getCommand() + " для предоставления пользователю прав администратора" +
                " (доступна только администраторам), например, " + Commands.GRANT_ADMIN_PRIVILEGES.getCommand() + " nickname " +
                "ADMIN (или USER)\n" +
                "* " + Commands.DELETE.getCommand() + " для удаления пользователя" +
                " (доступна только администраторам), например, " + Commands.DELETE.getCommand() + " nickname";
        System.out.println(commandsList);
    }
}

