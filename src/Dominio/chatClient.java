package Dominio;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;

public class chatClient {
    private static final String SERVER_ADDRESS = "localhost"; // Substitua pelo IP do servidor
    private static final int PORT = 12345;

    private JFrame frame;
    private JTextArea textArea;
    private JTextField textField;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String nick;

    public chatClient() {
        showNicknameInput();
    }

    private void showNicknameInput() {
        JFrame nicknameFrame = new JFrame("Enter Nickname");
        nicknameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        nicknameFrame.setLayout(new BorderLayout());

        JTextField nicknameField = new JTextField(20);
        JButton submitButton = new JButton("Submit");

        JPanel panel = new JPanel();
        panel.add(new JLabel("nickname:"));
        panel.add(nicknameField);
        panel.add(submitButton);

        nicknameFrame.add(panel, BorderLayout.CENTER);
        nicknameFrame.pack();
        nicknameFrame.setVisible(true);

        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                nick = nicknameField.getText().trim();
                if (nick.isEmpty()) {
                    JOptionPane.showMessageDialog(nicknameFrame, "Nickname cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    nicknameFrame.dispose();
                    startChat();
                }
            }
        });
    }

    private void startChat() {
        frame = new JFrame("Anonymus Chat");
        textArea = new JTextArea(15, 30);
        textField = new JTextField(30);
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);

        frame.setLayout(new BorderLayout());
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(textField, BorderLayout.SOUTH);

        textField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        connectToServer();
    }

    private void sendMessage() {
        String message = textField.getText();
        if (!message.isEmpty() && out != null) {
            String fullMessage = nick + ": " + message;
            System.out.println("Sending message: " + fullMessage);
            out.println(fullMessage);
            textField.setText("");
        }
    }

    private void receiveMessages() {
        String message;
        try {
            while ((message = in.readLine()) != null) {
                System.out.println("Received message: " + message);
                textArea.append(message + "\n");
            }
        } catch (IOException e) {
            System.err.println("Error receiving message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void connectToServer() {
        try {
            System.out.println("Connecting to server...");
            socket = new Socket(SERVER_ADDRESS, PORT);
            System.out.println("Connected to server.");

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Verifique se o PrintWriter não é nulo
            if (out != null) {
                System.out.println("PrintWriter initialized.");
            } else {
                System.err.println("Failed to initialize PrintWriter.");
            }

            Thread receiveThread = new Thread(this::receiveMessages);
            receiveThread.start();

        } catch (IOException e) {
            System.err.println("Error starting client: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
