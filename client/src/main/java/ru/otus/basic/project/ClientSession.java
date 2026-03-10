package ru.otus.basic.project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClientSession {
    private Scanner scanner;
    private SessionState state;
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private String nickname;
    private volatile boolean running = true;

    public ClientSession(Socket socket) {
        this.scanner = new Scanner(System.in);
        this.state = new AuthorizationState();
        this.socket = socket;
        try {
            reader = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public Scanner getScanner() {
        return scanner;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setState(SessionState state) {
        this.state = state;
    }

    public SessionState getState() {
        return state;
    }

    public void handleMessage() {
        while (running) {
            state.handleMessage(this);
        }
        disconnect();
    }

    public void stopHandling(){
        running = false;
    }

    public boolean isRunning(){
        return running;
    }

    public void sendMessage(String message) {
        writer.println(message);
    }

    public String getMessage() throws IOException {
        String message = reader.readLine();
        return (message == null) ? Commands.EXIT.getCommand() : message;
    }

    public void disconnect() {
        try {
            if (reader != null) {
                reader.close();
            }
            if (writer != null) {
                writer.close();
            }
            if (socket != null) {
                socket.close();
            }
            if (scanner != null) {
                scanner.close();
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
        System.out.printf("Клиент %s отключился", nickname);
    }

}
