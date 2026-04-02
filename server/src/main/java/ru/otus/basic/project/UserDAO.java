package ru.otus.basic.project;

import ru.otus.basic.project.Entities.User;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Интерфейс Data Access Object (DAO) для работы с таблицей зарегистрированных пользователей.
 * Определяет основные операции: создание таблицы, вставка, поиск, обновление роли, удаление.
 */
public interface UserDAO {

    void insertFirstAdmin() throws SQLException;

    Optional<User> insert(User user) throws SQLException;

    boolean findByNickname(String nickname);

    Optional<User> findByNicknameAndPassword(String nickname, String password) throws SQLException;

    Optional<Roles> getRoleByNickname(String nickname) throws SQLException;

    int updateRole(String nickname, Roles role) throws SQLException;

    int delete(String nickname) throws SQLException;
}
