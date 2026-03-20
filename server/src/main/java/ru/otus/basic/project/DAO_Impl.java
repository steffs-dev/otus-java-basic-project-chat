package ru.otus.basic.project;

import java.sql.*;

/**
 * Реализация интерфейса DAO для PostgreSQL.
 * Использует одно соединение с БД, полученное из класса Repository.
 * ResultSet, возвращаемый в методах, подлежит закрытию в месте получения
 */
public class DAO_Impl implements DAO {
    private final String initAdminName = "Admin";
    private final String initAdminPWD = "admin";
    Connection connection;

    /**
     * Конструктор создает репозиторий и получает из него соединение.
     *
     * @throws SQLException если не удается создать репозиторий (ошибка подключения)
     */
    public DAO_Impl() throws SQLException {
        Repository repository = new Repository();
        connection = repository.getConnection();
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
    @Override
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
        System.out.println("Table created!");
    }

    /**
     * Вставляет первую учетную запись администратора в таблицу.
     *
     * @throws SQLException при ошибке выполнения SQL. Не обрабатывается тут,
     *                      т.к. это критическая ошибка, влияющая на работу чата
     */
    @Override
    public void insertFirstAdmin() throws SQLException {
        PreparedStatement statement = connection.prepareStatement("" +
                "INSERT INTO reg_users (nickname, pwd, role)" +
                "VALUES (?, ?, ?)" +
                "ON CONFLICT (nickname) DO NOTHING;");
        statement.setString(1, initAdminName);
        statement.setString(2, initAdminPWD);
        statement.setObject(3, Roles.ADMIN.name(), Types.OTHER);

        statement.executeUpdate();
        statement.close();
        System.out.println("Admin inserted!");
    }

    /**
     * Добавляет нового пользователя с ролью USER (дефолтное значение) по умолчанию.
     *
     * @param nickname логин пользователя
     * @param password пароль пользователя
     * @return количество добавленных записей (ожидается 1 при успехе, 0 при ошибке)
     */
    @Override
    public int insert(String nickname, String password) {
        try (PreparedStatement statement = connection.prepareStatement("" +
                "INSERT INTO reg_users (nickname, pwd)" +
                "VALUES (?, ?);")) {
            statement.setString(1, nickname);
            statement.setString(2, password);
            return statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println(ConsoleColors.RED + "Ошибка при добавлении пользователя " +
                    nickname + " в БД" + ConsoleColors.RESET);
            e.printStackTrace();
            return 0;
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
        try (PreparedStatement statement = connection.prepareStatement("" +
                "SELECT nickname FROM reg_users WHERE nickname = ?;")) {
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
     * ResultSet отдельно не закрывается - будет закрыт при закрытии PreparedStatement
     * @param nickname логин
     * @param password пароль
     * @return true, если в БД найден пользователь с заданной парой ник-пароль, иначе false
     */
    @Override
    public boolean findByNicknameAndPassword(String nickname, String password) {
        try (PreparedStatement statement = connection.prepareStatement("" +
                "SELECT nickname FROM reg_users WHERE nickname = ? AND pwd = ?;")) {
            statement.setString(1, nickname);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Получает роль пользователя по логину.
     *
     * @param nickname логин
     * @return строка - значение, содержащееся в поле role или null при ошибке
     */
    @Override
    public String getRoleByNickname(String nickname) {
        try (PreparedStatement statement = connection.prepareStatement("" +
                "SELECT role FROM reg_users WHERE nickname = ?;")) {
            statement.setString(1, nickname);
            ResultSet resultSet = statement.executeQuery();
            String result = null;
            if (resultSet.next()) {
                result = resultSet.getString("role");
            }
            System.out.println("result = " + result);
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
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
    @Override
    public int updateRole(String nickname, Roles role) {
        try (PreparedStatement statement = connection.prepareStatement("" +
                "UPDATE reg_users SET role = ? WHERE nickname = ?;")) {
            statement.setObject(1, role.name(), Types.OTHER);
            statement.setString(2, nickname);
            return statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Удаляет пользователя из таблицы.
     *
     * @param nickname логин удаляемого пользователя
     * @return количество обновленных записей (1 при успехе, 0 при ошибке)
     */
    @Override
    public int delete(String nickname) {
        try (PreparedStatement statement = connection.prepareStatement("" +
                "DELETE FROM reg_users WHERE nickname = ?;")) {
            statement.setString(1, nickname);
            return statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

}
