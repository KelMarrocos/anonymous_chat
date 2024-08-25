package Test;

import Dominio.chatClient;
import Dominio.chatServer;

import javax.swing.*;

public class ChatApplication {
    public static void main(String[] args) {
        // Iniciar o servidor em uma nova thread
        chatServer server = new chatServer();
        new Thread(server::start).start();

        // Iniciar o cliente
        SwingUtilities.invokeLater(chatClient::new);
    }
}
