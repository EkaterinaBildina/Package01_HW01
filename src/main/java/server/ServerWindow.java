package server;

import client.ClientGUI;
import exceptions.LoginException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class ServerWindow extends JFrame{
    private static final int WIDTH = 400;
    private static final int HEIGHT = 300;

    private final JButton btnStart = new JButton("start dialog");
    private final JButton btnStop = new JButton("stop dialog");
    private final JTextArea informMessages = new JTextArea();
    private JTextArea historyUserMessages;
    private File messagesFile;
    private boolean isServerWorking;

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss ");

    private final Map<String, ClientGUI> authorizedUsers;

    public ServerWindow(){
        this.authorizedUsers = new HashMap<>();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);

        setTitle("Server Window");

        JPanel btnPanel = new JPanel(new GridLayout(1, 2));
        btnPanel.add(btnStart);
        btnPanel.add(btnStop);
        add(btnPanel, BorderLayout.SOUTH);
        informMessages.setEditable(false);
        add(new JScrollPane(informMessages), BorderLayout.CENTER);

        setVisible(true);

        isServerWorking = false;

        btnStart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                informMessages.append(LocalDateTime.now().format(dateTimeFormatter));
                if (isServerWorking) {
                    informMessages.append("server is working\n");
                } else {
                    isServerWorking = true;
                    historyUserMessages = initMessages();
                    informMessages.append("server started\n");
                }
            }
        });
        btnStop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isServerWorking) {
                    isServerWorking = false;
                    saveMessages(historyUserMessages);
                    disconnectUsers();
                    informMessages.append(LocalDateTime.now().format(dateTimeFormatter) + "server work stopped\n");
                } else {
                    informMessages.append(LocalDateTime.now().format(dateTimeFormatter) + "server isn't working\n");
                }
            }
        });

        }

    public JTextArea getMessages() {
        return historyUserMessages;
    }

    private JTextArea initMessages() {
        JTextArea messages = new JTextArea();
        messagesFile = new File("logFile.log");
        if (messagesFile.exists() && messagesFile.isFile()) {
            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(messagesFile))) {
                while (bufferedReader.ready()) {
                    messages.append(bufferedReader.readLine());
                    messages.append("\n");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return messages;
    }

    private void saveMessages(JTextArea userMessages) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(messagesFile))) {
            writer.write(userMessages.getText());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean authorize(ClientGUI clientWindow, String login, int passwordHash) throws  LoginException {
        if (isServerWorking) {
            if (authorizedUsers.containsKey(login)) {
                throw new LoginException(login + " name's already connected, please use another name.");
            }
            authorizedUsers.put(login, clientWindow);
            informMessages.append(LocalDateTime.now().format(dateTimeFormatter) + login + " connected\n");
            return true;
        }
        return false;
    }
    public void receiveMessage(String login, String message) {
        if (authorizedUsers.containsKey(login)) {
            updateMessages(LocalDateTime.now().format(dateTimeFormatter) + login + ": " + message + "\n");
        }
    }

    private void updateMessages(String message) {
        informMessages.append(message);
        historyUserMessages.append(message);
        for (ClientGUI client : authorizedUsers.values()) {
            client.newMessageFromServer(message);
        }
    }

    private void disconnectUsers() {
        for (String clientName : authorizedUsers.keySet()) {
            authorizedUsers.get(clientName).disconnectServer();
            informMessages.append(LocalDateTime.now().format(dateTimeFormatter) + clientName + " disconnected with server.\n");
        }
        authorizedUsers.clear();
    }
}
