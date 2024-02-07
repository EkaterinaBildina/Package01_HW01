package client;

import exceptions.LoginException;
import server.ServerWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

public class ClientGUI extends JFrame {
    private static final int WIDTH = 400;
    private static final int HEIGHT = 300;
    private final JTextArea log = new JTextArea();

    private final JTextField tfIPAddress = new JTextField("127.0.0.1");
    private final JTextField tfPort = new JTextField("8189");

    private final JTextField tfLogin = new JTextField("Ekaterina");
    private final JPasswordField tfPassword = new JPasswordField("123456");
    private final JButton btnLogin = new JButton("login");

    private JTextArea messages = new JTextArea();
    private final JTextField tfMessage = new JTextField();

    private final JButton btnSend = new JButton("send");

    private final JPanel loginOption;
    private final ServerWindow serverWindow;
    private final ClientGUI newChat;

    public ClientGUI(ServerWindow serverWindow) {
        newChat = this;
        this.serverWindow = serverWindow;

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setTitle("Chat client");

        JPanel serverParameters = new JPanel(new GridLayout(1, 3));
        serverParameters.add(tfIPAddress);
        serverParameters.add(tfPort);
        serverParameters.add(new Box(1));

        JPanel loginParameters = new JPanel(new GridLayout(1, 3));
        loginParameters.add(tfLogin);
        loginParameters.add(tfPassword);
        loginParameters.add(btnLogin);

        loginOption = new JPanel(new GridLayout(2, 1));
        loginOption.add(serverParameters);
        loginOption.add(loginParameters);
        add(loginOption, BorderLayout.NORTH);

        log.setEditable(false);
        JScrollPane scrollLog = new JScrollPane(log);
        add(scrollLog, BorderLayout.CENTER);

        JPanel sendMsgParameters = new JPanel(new BorderLayout());
        sendMsgParameters.add(tfMessage, BorderLayout.CENTER);
        sendMsgParameters.add(btnSend, BorderLayout.EAST);
        add(sendMsgParameters, BorderLayout.SOUTH);

        setVisible(true);

        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (serverWindow.authorize(newChat, tfLogin.getText(), Arrays.hashCode(tfPassword.getPassword()))) {
                        messages.append("Connection success!\n\n");
                        messages.append(serverWindow.getMessages().getText());
                        loginOption.setVisible(false);
                    } else {
                        messages.append("Connection failed\n");
                    }
                } catch (LoginException exception) {
                    messages.append(exception.getMessage());
                }
            }
        });

        btnSend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessageToServerAndClearField();
            }
        });

        tfMessage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessageToServerAndClearField();
            }
        });


    }

    private void sendMessageToServerAndClearField() {
        String message = tfMessage.getText();
        if (!message.isBlank()) {
            serverWindow.receiveMessage(tfLogin.getText(),
                    tfMessage.getText());
            tfMessage.setText("");
        }
    }
    public void newMessageFromServer(String message) {
        messages.append(message);
    }

    public void disconnectServer() {
        messages.append("Server work disconnected.\n");
        loginOption.setVisible(true);
    }

}
