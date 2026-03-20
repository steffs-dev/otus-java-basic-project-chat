package ru.otus.basic.project;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Сервис для работы с базой данных, использующий DAO.
 * Скрывает детали работы с ResultSet и обработку исключений.
 */
public class DBService {
    DAO dao;

    /**
     * Конструктор создает экземпляр DAO.
     *
     * @throws SQLException если не удается инициализировать DAO (проблемы с подключением)
     */
    public DBService() throws SQLException {
        dao = new DAO_Impl();
    }

    /**
     * Создает таблицу пользователей.
     *
     * @throws SQLException при ошибке выполнения SQL
     */
    public void createTable() throws SQLException {
        dao.createTable();
    }

    /**
     * Вставляет первую учетную запись администратора.
     *
     * @throws SQLException при ошибке выполнения SQL
     */
    public void insertFirstAdmin() throws SQLException {
        dao.insertFirstAdmin();
    }

    /**
     * Добавляет нового пользователя.
     *
     * @param nickname логин
     * @param password пароль
     * @return количество добавленных записей (1 при успехе, 0 при ошибке)
     */
    public int insert(String nickname, String password) {
        return dao.insert(nickname, password);
    }

    /**
     * Проверяет, существует ли пользователь с указанным логином.
     *
     * @param nickname логин
     * @return true, если пользователь найден, иначе false
     */
    public boolean findByNickname(String nickname) {
        return dao.findByNickname(nickname);
    }

    /**
     * Проверяет правильность пары логин/пароль для авторизации.
     *
     * @param nickname логин
     * @param password пароль
     * @return true, если пользователь с такими данными существует, иначе false
     */
    public boolean findByNicknameAndPassword(String nickname, String password) {
        return dao.findByNicknameAndPassword(nickname, password);
    }

    /**
     * Возвращает роль пользователя по логину.
     *
     * @param nickname логин
     * @return роль пользователя или null, если пользователь не найден или произошла ошибка
     */
    public Roles getRoleByNickname(String nickname) {
        try {
            return Roles.valueOf(dao.getRoleByNickname(nickname));
        } catch (IllegalArgumentException e) {
            System.out.println(ConsoleColors.RED + "Неизвестная роль в выдаче из БД по нику: " +
                    nickname + ConsoleColors.RESET);
            return null;
        }
    }

    /**
     * Обновляет роль пользователя.
     *
     * @param nickname логин
     * @param role     новая роль
     * @return количество обновленных записей (1 при успехе, 0 при ошибке)
     */
    public int updateRole(String nickname, Roles role) {
        return dao.updateRole(nickname, role);
    }

    /**
     * Удаляет пользователя.
     *
     * @param nickname логин
     * @return количество удаленных записей (в текущей реализации всегда 0)
     */
    public int delete(String nickname) {
        return dao.delete(nickname);
    }
}
