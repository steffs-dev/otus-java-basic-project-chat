package ru.otus.basic.project;

public class ExitState implements SessionState {

    @Override
    public void handleMessage(ClientSession clientSession) {
        System.out.println("До свидания!");
        clientSession.disconnect();
    }
}
