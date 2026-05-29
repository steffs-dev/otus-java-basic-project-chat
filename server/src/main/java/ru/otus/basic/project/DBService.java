package ru.otus.basic.project;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.otus.basic.project.Entities.User;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Сервис для работы с базой данных, использующий DAO.
 * Скрывает детали работы с ResultSet и обработку исключений.
 */
public class DBService {
    UserDAO userDao;

    private static final Logger logger = LogManager.getLogger(DBService.class);

    /**
     * Конструктор создает экземпляр DAO.
     *
     * @throws SQLException если не удается инициализировать DAO (проблемы с подключением)
     */
    public DBService() throws SQLException {
        userDao = new UserDAO_Impl();
    }

    /**
     * Вставляет первую учетную запись администратора.
     *
     * @throws SQLException при ошибке выполнения SQL
     */
    public void insertFirstAdmin() throws SQLException {
        userDao.insertFirstAdmin();
    }

    /**
     * Добавляет нового пользователя.
     *
     * @param nickname логин
     * @param password пароль
     * @return Optional<User> с установленным id, если успешно, иначе пустой Optional
     */
    public Optional<User> insert(String nickname, String password) {
            if (findByNickname(nickname)) {
                return Optional.empty();
            }
            User user = new User(nickname, password, Roles.USER);
            return userDao.insert(user);
    }

    /**
     * Проверяет, существует ли пользователь с указанным логином.
     *
     * @param nickname логин
     * @return true, если пользователь найден, иначе false
     */
    public boolean findByNickname(String nickname) {
            return userDao.findByNickname(nickname);
    }

    /**
     * Проверяет правильность пары логин/пароль для авторизации.
     *
     * @param nickname логин
     * @param password пароль
     * @return true, если пользователь с такими данными существует, иначе false
     */
    public Optional<User> findByNicknameAndPassword(String nickname, String password) {
            return userDao.findByNicknameAndPassword(nickname, password);
    }

    /**
     * Возвращает роль пользователя по логину.
     *
     * @param nickname логин
     * @return роль пользователя или null, если пользователь не найден или произошла ошибка
     */
    public Roles getRoleByNickname(String nickname) {
        try {
            Optional<Roles> rolesOptional = userDao.getRoleByNickname(nickname);
            return rolesOptional.orElse(null);
        } catch (IllegalArgumentException e) {
            logger.error("Unknown role received from DB by nickname {}. {}",
                    nickname, e.getMessage());
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
            return userDao.updateRole(nickname, role);
    }

    /**
     * Удаляет пользователя.
     *
     * @param nickname логин
     * @return количество удаленных записей (в текущей реализации всегда 0)
     */
    public int delete(String nickname) {
            return userDao.delete(nickname);
    }
}
