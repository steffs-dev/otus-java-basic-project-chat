package ru.otus.basic.project;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Репозиторий для работы с базой данных.
 * Содержит единственное соединение с БД, которое создается при инициализации.
 * Потокобезопасность обеспечивается на уровне драйвера.
 * При расширении функционала и нагрузки может быть внедрен ConnectionPool с отдельным Connection на уровне ClientHandler
 * Подключение осуществляется с базой postgre
 */
public class Repository {
    private final String URL = "jdbc:postgresql://localhost:5432/console_chat_db";
    private final String LOGIN = "postgres";
    private final String PWD = "admin";
    Connection connection;

    /**
     * Создает репозиторий и устанавливает соединение с БД.
     *
     * @throws SQLException если не удается подключиться к базе данных. Не обрабатывается тут,
     *                      т.к. это критическая ошибка, влияющая на работу чата
     */
    public Repository() throws SQLException {
        this.connection = DriverManager.getConnection(URL, LOGIN, PWD);
    }

    /**
     * Возвращает текущее соединение с БД.
     *
     * @return объект Connection
     */
    public Connection getConnection() {
        return connection;
    }
}
