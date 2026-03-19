package ru.otus.basic.project;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Класс, отвечающий за безопасность: хранение учетных данных пользователей и их ролей.
 * authPairs - мапа для хранения учетных данных зарегистрированных пользователей
 * ник - пара пароль, роль.
 * Добавление в мапу происходит при регистрации пользователя, удаление - в результате исполнения
 * команды /delete
 */
public class Security {
    private Map<String, Pair> authPairs;

    /**
     * Создает хранилище и добавляет первого администратора с ролью Admin.
     */
    public Security() {
        authPairs = new ConcurrentHashMap<>();
        addSupervisor();
    }

    /**
     * Проверяет, зарегистрирован ли пользователь с таким ником (есть ли такой ключ в мапе).
     *
     * @param nickname логин
     * @return true, если есть запись
     */
    public boolean isRegistered(String nickname) {
        return authPairs.containsKey(nickname);
    }

    /**
     * Проверяет правильность пароля для данного логина. Используется для входа в чат
     * зарегистрированного пользователя
     *
     * @param nickname логин
     * @param password пароль
     * @return true, если пароль верен
     */
    public boolean isAuthenticated(String nickname, String password) {
        if (isRegistered(nickname)) {
            return authPairs.get(nickname).getPassword().equals(password);
        } else {
            return false;
        }
    }

    /**
     * Возвращает роль пользователя.
     *
     * @param nickname логин
     * @return роль
     */
    public Roles getRole(String nickname) {
        return authPairs.get(nickname).getRole();
    }

    /**
     * Устанавливает роль пользователя.
     *
     * @param nickname логин
     * @param role     новая роль
     */
    public void setRole(String nickname, Roles role) {
        authPairs.get(nickname).setRole(role);
    }

    /**
     * Удаляет учётную запись пользователя.
     *
     * @param nickname логин
     */
    public void deleteAuthPair(String nickname) {
        authPairs.remove(nickname);
    }

    /**
     * Добавляет нового пользователя с дефолтной ролью USER.
     *
     * @param nickname логин
     * @param password пароль
     */
    public void addClientCredentials(String nickname, String password) {
        authPairs.putIfAbsent(nickname, new Pair(password));
    }

    /**
     * Добавляет администратора по умолчанию.
     */
    private void addSupervisor() {
        authPairs.put("Admin", new Pair("admin", Roles.ADMIN));
    }

    /**
     * Внутренний класс, хранящий пару (пароль, роль) для одного пользователя.
     * password - пароль пользователя
     * role - роль пользователя
     */
    private class Pair {
        private String password;
        private Roles role;

        /**
         * Создает пару с заданным пользователем паролем и дефолтной ролью USER.
         */
        public Pair(String password) {
            this.password = password;
            role = Roles.USER;
        }

        /**
         * Создает пару с заданным пользователем паролем и заданной ролью.
         * Используется для добавления администратора по умолчанию.
         */
        public Pair(String password, Roles role) {
            this.password = password;
            this.role = role;
        }

        /**
         * Геттер для пароля
         *
         * @return password
         */
        private String getPassword() {
            return password;
        }

        /**
         * Геттер для роли
         *
         * @return role
         */
        private Roles getRole() {
            return role;
        }

        /**
         * Сеттер для роли
         *
         * @param role
         */
        private void setRole(Roles role) {
            this.role = role;
        }
    }
}
