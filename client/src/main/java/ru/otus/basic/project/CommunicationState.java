package ru.otus.basic.project;

public class CommunicationState implements SessionState {

    @Override
    public void handleMessage(ClientSession clientSession) {
        Thread readerThread = new Thread(() -> {
            while (clientSession.isRunning()) {
                try {
                    String message = clientSession.getMessage();
                    if (message == null) {
                        break;
                    }
                    if (message.startsWith("/")) {
                        handleCommands(clientSession, message);
                        }
                    System.out.println(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        readerThread.start();

        while (clientSession.isRunning()) {
            String message = clientSession.getScanner().nextLine().trim();
            clientSession.sendMessage(message);
            if (message.startsWith("/")) {
                handleCommands(clientSession, message);
                if (clientSession.getState() instanceof ExitState) {
                    clientSession.stopHandling();
                    break;
                }
            }
        }
        try{
            readerThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
    }

    public synchronized void handleCommands(ClientSession clientSession, String command) {
        String[] split = command.split(" ");
        if (split[0].equals(Commands.EXIT.getCommand())) {
            clientSession.setState(new ExitState());
        }
    }
}
