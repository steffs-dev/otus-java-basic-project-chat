package ru.otus.basic.project;

/**
 * Интерфейс состояния сессии клиента (клиент реализован в соответствии с паттерном State)
 * Каждое состояние реализует свою логику обработки сообщений
 */
public interface SessionState {
    void handleMessage(ClientSession clientSession);
}
