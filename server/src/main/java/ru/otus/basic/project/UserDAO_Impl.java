package ru.otus.basic.project;

import ru.otus.basic.project.Entities.User;

import java.sql.*;
import java.util.Optional;

/**
 * Реализация интерфейса DAO для PostgreSQL.
 * Использует одно соединение с БД, полученное из класса Repository.
 */
public class UserDAO_Impl implements UserDAO {
    private final String initAdminName = "Admin";
    private final String initAdminPWD = "admin";
    Connection connection;

    /**
     * Конструктор создает репозиторий и получает из него соединение.
     *
     * @throws SQLException если не удается создать репозиторий (ошибка подключения)
     */
    public UserDAO_Impl() throws SQLException {
        Repository repository = new Repository();
        connection = repository.getConnection();
    }

    /**
     * Вставляет первую учетную запись администратора в таблицу.
     *
     * @throws SQLException при ошибке выполнения SQL. Не обрабатывается тут,
     *                      т.к. это критическая ошибка, влияющая на работу чата
     */
    @Override
    public void insertFirstAdmin() throws SQLException {
        User admin = new User(initAdminName, initAdminPWD, Roles.ADMIN);
        insert(admin);
        System.out.println("Admin inserted!");
    }

    /**
     * Добавляет нового пользователя с ролью USER (дефолтное значение) по умолчанию.
     *
     * @param user пользователь
     * @return количество добавленных записей (ожидается 1 при успехе, 0 при ошибке)
     */
    @Override
    public Optional<User> insert(User user) throws SQLException {
        String sql = "" +
                "INSERT INTO reg_users (nickname, pwd, role) " +
                "VALUES (?, ?, ?::roles) " +
                "ON CONFLICT (nickname) DO NOTHING;";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, user.getName());
            statement.setString(2, user.getPassword());
            statement.setObject(3, user.getRole().name(), Types.OTHER);
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getLong(1));
                    return Optional.of(user);
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * Ищет пользователя по логину.
     * ResultSet отдельно не закрывается - будет закрыт при закрытии PreparedStatement
     * @param nickname логин для поиска
     * @return true, если в БД найден пользователь с заданным ником, иначе false
     */
    @Override
    public boolean findByNickname(String nickname) {
        String sql = "SELECT nickname FROM reg_users WHERE nickname = ?;";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nickname);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Ищет пользователя по логину и паролю (для аутентификации).
     *
     * @param nickname логин
     * @param password пароль
     * @return true, если в БД найден пользователь с заданной парой ник-пароль, иначе false
     */
    @Override
    public Optional<User> findByNicknameAndPassword(String nickname, String password) {
        String sql = "SELECT id, nickname, pwd, role FROM reg_users WHERE nickname = ? AND pwd = ?;";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nickname);
            statement.setString(2, password);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapToUser(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * Получает роль пользователя по логину.
     *
     * @param nickname логин
     * @return строка - значение, содержащееся в поле role или null при ошибке
     */
    @Override
    public Optional<Roles> getRoleByNickname(String nickname) {
        String sql = "SELECT role FROM reg_users WHERE nickname = ?;";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nickname);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(Roles.valueOf(resultSet.getString("role")));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * Обновляет роль пользователя.
     *
     * @param nickname логин
     * @param role     новая роль
     * @return количество обновленных записей (1 при успехе, 0 при ошибке)
     */
    @Override
    public int updateRole(String nickname, Roles role) throws SQLException {
        String sql = "UPDATE reg_users SET role = ? WHERE nickname = ?;";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, role.name(), Types.OTHER);
            statement.setString(2, nickname);
            return statement.executeUpdate();
        }
    }

    /**
     * Удаляет пользователя из таблицы.
     *
     * @param nickname логин удаляемого пользователя
     * @return количество обновленных записей (1 при успехе, 0 при ошибке)
     */
    @Override
    public int delete(String nickname) throws SQLException {
        String sql = "DELETE FROM reg_users WHERE nickname = ?;";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nickname);
            return statement.executeUpdate();
        }
    }

    /**
     * Создает объект пользователя из строки ResultSet
     *
     * @param resultSet результат селекта
     * @return объект пользователя
     * @throws SQLException
     */
    private User mapToUser(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setId(resultSet.getLong("id"));
        user.setName(resultSet.getString("nickname"));
        user.setPassword(resultSet.getString("pwd"));
        user.setRole(Roles.valueOf(resultSet.getString("role")));
        return user;
    }
}
