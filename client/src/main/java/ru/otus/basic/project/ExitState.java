package ru.otus.basic.project;

/**
 * Класс состояния выхода из чата.
 * Выводит прощальное сообщение и закрывает соединение.
 */
public class ExitState implements SessionState {

    /**
     * Завершает сессию: выводит сообщение и закрывает ресурсы.
     *
     * @param clientSession сессия клиента
     */
    @Override
    public void handleMessage(ClientSession clientSession) {
        System.out.println(ConsoleColors.WHITE_UNDERLINED + "До свидания!" + ConsoleColors.RESET);
        clientSession.disconnect();
    }
}
