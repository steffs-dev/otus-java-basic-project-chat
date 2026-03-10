package ru.otus.basic.project;

public enum Roles {
    USER ("USER"),
    ADMIN ("ADMIN");
    private String roleDescription;
    Roles(String roleDescription) {
        this.roleDescription = roleDescription;
    }
    public String getRoleDescription() {
        return roleDescription;
    }
}
