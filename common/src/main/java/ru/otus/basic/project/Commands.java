package ru.otus.basic.project;

public enum Commands {

    LOGIN ("/login"),
    REGISTER ("/reg"),
    WRITE_DIRECT ("/w"),
    LOGOUT ("/kick"),
    DELETE ("/delete"),
    GRANT_ADMIN_PRIVILEGES("/gadmin"),
    EXIT ("/exit");

    private String command;
    Commands(String command) {
        this.command = command;
    }
    public String getCommand() {
        return command;
    }
}
