package ru.otus.basic.project;

/**
 * Роли пользователей в чате
 */
public enum Roles {
    USER ("USER"),    // пользователь
    ADMIN ("ADMIN");  // администратор (может быть несколько)

    private final String roleDescription;

    Roles(String roleDescription) {
        this.roleDescription = roleDescription;
    }

    public String getRoleDescription() {
        return roleDescription;
    }
}
