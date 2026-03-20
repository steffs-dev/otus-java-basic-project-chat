package ru.otus.basic.project;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Интерфейс Data Access Object (DAO) для работы с таблицей зарегистрированных пользователей.
 * Определяет основные операции: создание таблицы, вставка, поиск, обновление роли, удаление.
 */
public interface DAO {

    void createTable() throws SQLException;

    void insertFirstAdmin() throws SQLException;

    int insert(String nickname, String password);

    boolean findByNickname(String nickname);

    boolean findByNicknameAndPassword(String nickname, String password);

    String getRoleByNickname(String nickname);

    int updateRole(String nickname, Roles role);

    int delete(String nickname);
}
