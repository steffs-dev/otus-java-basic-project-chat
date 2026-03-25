package ru.otus.basic.project;

import ru.otus.basic.project.Entities.User;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Сервис для работы с базой данных, использующий DAO.
 * Скрывает детали работы с ResultSet и обработку исключений.
 */
public class DBService {
    UserDAO userDao;

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
        try {
            if (findByNickname(nickname)) {
                return Optional.empty();
            }
            User user = new User(nickname, password, Roles.USER);
            return userDao.insert(user);
        } catch (SQLException e){
            System.out.println(ConsoleColors.RED + "Ошибка при добавлении пользователя " +
                    nickname + " в БД" + ConsoleColors.RESET);
            e.printStackTrace();
            return Optional.empty();
        }
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
        try {
            return userDao.findByNicknameAndPassword(nickname, password);
        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        }
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
            if (rolesOptional.isPresent()) {
                return rolesOptional.get();
            }
            return null;
        } catch (IllegalArgumentException e) {
            System.out.println(ConsoleColors.RED + "Неизвестная роль в выдаче из БД по нику: " +
                    nickname + ConsoleColors.RESET);
            return null;
        } catch (SQLException e) {
            System.out.println(ConsoleColors.RED + "Ошибка при получении роли пользователя " +
                    nickname + " из БД" + ConsoleColors.RESET);
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
        try{
            return userDao.updateRole(nickname, role);
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Удаляет пользователя.
     *
     * @param nickname логин
     * @return количество удаленных записей (в текущей реализации всегда 0)
     */
    public int delete(String nickname) {
        try {
            return userDao.delete(nickname);
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
