package ru.otus.basic.project;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
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

    private static final Logger logger = LogManager.getLogger(Repository.class);

    /**
     * Создает репозиторий и устанавливает соединение с БД.
     *
     * @throws SQLException если не удается подключиться к базе данных. Не обрабатывается тут,
     *                      т.к. это критическая ошибка, влияющая на работу чата
     */
    public Repository() throws SQLException {
        this.connection = DriverManager.getConnection(URL, LOGIN, PWD);
        try {
            createTable();
        } catch (SQLException e) {
            logger.error("Failed to create user table in repository. {}",  e.getMessage());
            throw new RuntimeException("Ошибка при создании таблицы пользователей. " + e);
        }
    }

    /**
     * Возвращает текущее соединение с БД.
     *
     * @return объект Connection
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Закрывает подключение
     * @throws SQLException
     */
    public void closeConnection() throws SQLException {
        connection.close();
    }

    /**
     * Создает таблицу reg_users, если она еще не существует.
     * Таблица содержит поля: id (автоинкремент), nickname (уникальный), pwd (уникальный), role (enum).
     * Дефолтное значение для поля role - USER.
     * Индекс на nickname не установлен, т.к. устанавливается автоматически в связи с констрейнтом UNIQUE на поле
     * В БД предварительно создан enum ролей: CREATE TYPE roles AS ENUM ('USER', 'ADMIN');
     *
     * @throws SQLException при ошибке выполнения SQL. Не обрабатывается тут,
     *                      т.к. это критическая ошибка, влияющая на работу чата
     */
    public void createTable() throws SQLException {
        PreparedStatement statement = connection.prepareStatement("" +
                "CREATE TABLE IF NOT EXISTS reg_users (" +
                "id BIGSERIAL PRIMARY KEY," +
                "nickname VARCHAR(255) UNIQUE NOT NULL," +
                "pwd VARCHAR(255) NOT NULL," +
                "role roles DEFAULT 'USER'," +
                "CONSTRAINT not_empty_credentials CHECK(nickname != '' OR pwd != '')" +
                ");");
        statement.executeUpdate();
        statement.close();
        logger.info("Table reg_users created in DB");
    }
}
