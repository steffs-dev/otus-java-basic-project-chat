package ru.otus.basic.project;

import java.io.IOException;

/**
 * Класс состояния авторизации клиента.
 * Отвечает за взаимодействие с пользователем на этапе входа или регистрации.
 * Запрашивает логин и пароль, отправляет соответствующие команды на сервер
 * и обрабатывает ответы.
 * nickname - ник, введенный пользователем
 * password - пароль, введенный пользователем
 */
public class AuthorizationState implements SessionState {
    private String nickname;
    private String password;

    /**
     * Основной цикл обработки сообщений в состоянии авторизации.
     * Предлагает пользователю выбрать действие (регистрация, вход, возврат),
     * затем вызывает соответствующий метод обработки.
     *
     * @param clientSession текущая сессия клиента
     */
    @Override
    public void handleMessage(ClientSession clientSession) {
        while (true) {
            System.out.println(ConsoleColors.WHITE + "Доброе пожаловать в чат. Для регистрации введите " + ConsoleColors.BLUE_BOLD +
                    Commands.REGISTER.getCommand() + ConsoleColors.WHITE + ", для авторизации введите " + ConsoleColors.BLUE_BOLD +
                    Commands.LOGIN.getCommand() + ConsoleColors.WHITE + ", для возврата введите " +
                    ConsoleColors.BLUE_BOLD + Commands.BACK.getCommand() + ConsoleColors.RESET);
            try {
                String command = clientSession.getConsoleReader().readLine().trim();

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
                    System.out.println(ConsoleColors.RED + "Неверная команда. Введите" + ConsoleColors.BLUE_BOLD +
                            Commands.REGISTER.getCommand() + ConsoleColors.RED + " или " +
                            ConsoleColors.BLUE_BOLD + Commands.LOGIN.getCommand() + ConsoleColors.RESET);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Форма для заполнения данных для входа в чат.
     * Запрашивает у пользователя логин и пароль с возможностью возврата в первоначальное меню,
     * если ошибочно вошел в регистрацию или авторизацию (команда возврата может быть введена
     * как на этапе ввода логина, так и пароля)
     *
     * @param clientSession сессия клиента
     * @return true, если пользователь ввел команду возврата, иначе false
     * @throws IOException при ошибке ввода-вывода
     */
    private boolean authorizationForm(ClientSession clientSession) throws IOException {
        boolean goBack = false;
        nickname = null;
        while (nickname == null || nickname.isBlank()) {
            System.out.println(ConsoleColors.WHITE_UNDERLINED + "Введите логин:" + ConsoleColors.RESET);
            String inputName = clientSession.getConsoleReader().readLine().trim();
            if (inputName.contains(" ")) {
                System.out.println(ConsoleColors.RED + "Логин не должен содержать пробелы." + ConsoleColors.RESET);
                continue;
            }

            if (inputName.equals(Commands.BACK.getCommand())) {
                goBack = true;
                return goBack;
            }
            nickname = inputName;
        }

        password = null;
        while (password == null || password.isBlank()) {
            System.out.println(ConsoleColors.WHITE_UNDERLINED + "Введите пароль:" + ConsoleColors.RESET);
            String inputPwd = clientSession.getConsoleReader().readLine().trim();
            if (inputPwd.contains(" ")) {
                System.out.println(ConsoleColors.RED + "Пароль не должен содержать пробелы." + ConsoleColors.RESET);
                continue;
            }

            if (inputPwd.equals(Commands.BACK.getCommand())) {
                goBack = true;
                return goBack;
            }
            password = inputPwd;
        }
        clientSession.setNickname(nickname);
        System.out.println(ConsoleColors.WHITE_UNDERLINED + "Вы ввели логин: " + ConsoleColors.BLUE_BOLD +
                nickname + ConsoleColors.WHITE_UNDERLINED + ", пароль: " + ConsoleColors.BLUE_BOLD +
                password + ConsoleColors.RESET);
        return goBack;
    }

    /**
     * Если в форме (authorizationForm) не введена команда для возврата, то метод
     * отправляет на сервер команду для авторизации с введенными данными (логин, пароль).
     *
     * @param clientSession сессия клиента
     * @return true, если пользователь решил вернуться, иначе false
     * @throws IOException при ошибке ввода-вывода
     */
    private boolean authorization(ClientSession clientSession) throws IOException {
        boolean goBack = false;
        if (authorizationForm(clientSession)) {
            goBack = true;
            return goBack;
        }
        clientSession.sendMessage(Commands.LOGIN.getCommand() + " " + nickname + " " + password);
        return goBack;
    }

    /**
     * Если в форме (authorizationForm) не введена команда для возврата, то метод
     * отправляет на сервер команду регистрации с введенными данными (логин, пароль).
     *
     * @param clientSession сессия клиента
     * @return true, если пользователь решил вернуться, иначе false
     * @throws IOException при ошибке ввода-вывода
     */
    private boolean registration(ClientSession clientSession) throws IOException {
        boolean goBack = false;
        if (authorizationForm(clientSession)) {
            goBack = true;
            return goBack;
        }
        clientSession.sendMessage(Commands.REGISTER.getCommand() + " " + nickname + " " + password);
        return goBack;
    }

    /**
     * Выполняет процесс регистрации: отправляет данные на сервер (метод registration) и ожидает ответа.
     * При успехе переводит клиента в состояние общения (CommunicationState).
     *
     * @param clientSession сессия клиента
     * @return true, если регистрация успешна, иначе false
     * @throws IOException при ошибке ввода-вывода
     */
    private boolean processRegistration(ClientSession clientSession) throws IOException {
        boolean isRegSucceed = false;
        while (true) {
            if (registration(clientSession)) {
                return isRegSucceed;
            }

            try {
                String response = clientSession.getMessage();
                if (response == null) {
                    System.out.println(ConsoleColors.RED + "Соединение с сервером разорвано." + ConsoleColors.RESET);
                    clientSession.setState(new ExitState());
//                    clientSession.stopHandling();
                    return isRegSucceed;
                }

                switch (response) {
                    case "loginCompleted" -> {
                        System.out.println(ConsoleColors.GREEN + "Добро пожаловать в чат, " +
                                ConsoleColors.GREEN_BOLD + nickname + ConsoleColors.RESET);
                        printCommands();
                        isRegSucceed = true;
                        return isRegSucceed;
                    }
                    case "registrationFailed" -> {
                        System.out.println(ConsoleColors.RED + "Пользователь с ником " + nickname +
                                " уже зарегистрирован. Выберите другой ник и повторите попытку" +
                                ConsoleColors.RESET);
                    }
                    default -> {
                        System.out.println(ConsoleColors.RED + "Неизвестный ответ от сервера. " +
                                "Попробуйте снова." + ConsoleColors.RESET);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                clientSession.setState(new ExitState());
//                clientSession.stopHandling();
                return isRegSucceed;
            }
        }
    }

    /**
     * Выполняет процесс входа: отправляет данные на сервер (метод authorization) и ожидает ответа.
     * При успехе переводит клиента в состояние общения (CommunicationState).
     *
     * @param clientSession сессия клиента
     * @return true, если вход успешен, иначе false
     * @throws IOException при ошибке ввода-вывода
     */
    private boolean processLogin(ClientSession clientSession) throws IOException {
        boolean isLoginSucceed = false;
        while (true) {
            if (authorization(clientSession)) {
                return isLoginSucceed;
            }

            try {
                String response = clientSession.getMessage();
                if (response == null) {
                    System.out.println(ConsoleColors.RED + "Соединение с сервером разорвано." + ConsoleColors.RESET);
                    clientSession.setState(new ExitState());
//                    clientSession.stopHandling();
                    return isLoginSucceed;
                }

                switch (response) {
                    case "loginCompleted" -> {
                        System.out.println(ConsoleColors.GREEN + "Добро пожаловать в чат, " +
                                ConsoleColors.GREEN_BOLD + nickname + ConsoleColors.RESET);
                        printCommands();
                        isLoginSucceed = true;
                        return isLoginSucceed;
                    }
                    case "authFailedAlreadyLoggedIn" -> {
                        System.out.println(ConsoleColors.RED + "Пользователь с ником " + nickname +
                                " уже вошел в чат" + ConsoleColors.RESET);
                    }
                    case "authFailed" -> {
                        System.out.println(ConsoleColors.RED + "Неверный логин и/или пароль. Повторите попытку." + ConsoleColors.RESET);
                    }
                    default -> {
                        System.out.println(ConsoleColors.RED + "Неизвестный ответ от сервера. Попробуйте снова." + ConsoleColors.RESET);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                clientSession.setState(new ExitState());
//                clientSession.stopHandling();
                return isLoginSucceed;
            }
        }
    }

    /**
     * Выводит список доступных команд с описанием.
     */
    private void printCommands() {
        String commandsList = ConsoleColors.WHITE_UNDERLINED + "Для управления могут быть использованы следующие команды:\n" +
                ConsoleColors.YELLOW + "* " + ConsoleColors.BLUE_BOLD + Commands.REGISTER.getCommand() +
                ConsoleColors.WHITE + " - для регистрации, например," +
                ConsoleColors.GREEN_UNDERLINED + Commands.REGISTER.getCommand() + " nickname password\n" + ConsoleColors.RESET +

                ConsoleColors.YELLOW + "* " + ConsoleColors.BLUE_BOLD + Commands.LOGIN.getCommand() +
                ConsoleColors.WHITE + " - для входа в учетную запись, например, " +
                ConsoleColors.GREEN_UNDERLINED + Commands.LOGIN.getCommand() + " nickname password\n" + ConsoleColors.RESET +

                ConsoleColors.YELLOW + "* " + ConsoleColors.BLUE_BOLD + Commands.BACK.getCommand() +
                ConsoleColors.WHITE + " - если вы ошибочно вошли в логирование или в раздел регистрации, введите" +
                ConsoleColors.GREEN_UNDERLINED + Commands.BACK.getCommand() + " для возврата на этап выбора способа входа\n" + ConsoleColors.RESET +

                ConsoleColors.YELLOW + "* " + ConsoleColors.BLUE_BOLD + Commands.WRITE_DIRECT.getCommand() +
                ConsoleColors.WHITE + " - для направления персонального сообщения, например, " +
                ConsoleColors.GREEN_UNDERLINED + Commands.WRITE_DIRECT.getCommand() + " nickname(addressee) text\n" + ConsoleColors.RESET +

                ConsoleColors.YELLOW + "* " + ConsoleColors.BLUE_BOLD + Commands.EXIT.getCommand() +
                ConsoleColors.WHITE + " - для выхода из чата, например, " +
                ConsoleColors.GREEN_UNDERLINED + Commands.EXIT.getCommand() + "\n" + ConsoleColors.RESET +

                ConsoleColors.YELLOW + "* " + ConsoleColors.BLUE_BOLD + Commands.MY_INFO.getCommand() +
                ConsoleColors.WHITE + " - для получения своих учетных данных, например, " +
                ConsoleColors.GREEN_UNDERLINED + Commands.MY_INFO.getCommand() + "\n" + ConsoleColors.RESET +

                ConsoleColors.YELLOW + "* " + ConsoleColors.BLUE_BOLD + Commands.LOGOUT.getCommand() +
                ConsoleColors.WHITE + " - для завершения сессии пользователя (доступна только администраторам), например, " +
                ConsoleColors.GREEN_UNDERLINED + Commands.LOGOUT.getCommand() + " nickname\n" + ConsoleColors.RESET +

                ConsoleColors.YELLOW + "* " + ConsoleColors.BLUE_BOLD + Commands.GRANT_PRIVILEGES.getCommand() +
                ConsoleColors.WHITE + " - для изменения роли пользователя (доступна только администраторам), например, " +
                ConsoleColors.GREEN_UNDERLINED + Commands.GRANT_PRIVILEGES.getCommand() + " nickname " +
                "роль (например, ADMIN (или USER))\n" + ConsoleColors.RESET +

                ConsoleColors.YELLOW + "* " + ConsoleColors.BLUE_BOLD + Commands.DELETE.getCommand() +
                ConsoleColors.WHITE + " - для удаления пользователя (доступна только администраторам), например, " +
                ConsoleColors.GREEN_UNDERLINED + Commands.DELETE.getCommand() + " nickname" + ConsoleColors.RESET;

        System.out.println(commandsList);
    }
}

