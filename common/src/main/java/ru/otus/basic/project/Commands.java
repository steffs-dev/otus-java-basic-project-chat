package ru.otus.basic.project;

/**
 * Перечисление всех команд, используемых в чате.
 * Каждая команда имеет строковое представление (например, "/login").
 */
public enum Commands {

    LOGIN ("/login"),
    REGISTER ("/reg"),
    WRITE_DIRECT ("/w"),
    LOGOUT ("/kick"),
    DELETE ("/delete"),
    GRANT_PRIVILEGES("/gpriv"),
    EXIT ("/exit"),
    BACK ("/back"),
    MY_INFO("/myinfo"),
    UNKNOWN ("");

    private final String command;

    Commands(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }
}
