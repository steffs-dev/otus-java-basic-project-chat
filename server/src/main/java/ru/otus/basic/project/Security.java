package ru.otus.basic.project;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Security {
    private Map<String, Pair> authPairs;

    public Security() {
        authPairs = new ConcurrentHashMap<>();
    }

    public boolean isRegistered(String nickname) {
        return authPairs.containsKey(nickname);
    }

    public boolean isAuthenticated(String nickname, String password) {
        if (isRegistered(nickname)) {
            return authPairs.get(nickname).getPassword().equals(password);
        } else  {
            return false;
        }
    }

    public Roles getRole (String nickname){
        return authPairs.get(nickname).getRole();
    }

    public void setRole(String nickname, Roles role) {
        authPairs.get(nickname).setRole(role);
    }

    public void deleteAuthPair(String nickname){
        authPairs.remove(nickname);
    }

    public void addClientCredentials(String nickname, String password) {
        authPairs.put(nickname, new Pair(password));
        addSupervisor(nickname, password);
    }

    private void addSupervisor(String nickname, String password) {
        if (nickname.equals("supervisor") && password.equals("qaz")) {
            authPairs.get(nickname).setRole(Roles.ADMIN);
        }
    }

    private class Pair {
        private String password;
        private Roles role;

        public Pair(String password) {
            this.password = password;
            role = Roles.USER;
        }

        private String getPassword() {
            return password;
        }

        private Roles getRole() {
            return role;
        }

        private void setRole(Roles role) {
            this.role = role;
        }
    }
}
