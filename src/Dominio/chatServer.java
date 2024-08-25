package Dominio;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class chatServer {
    private static final int PORT = 12345;
    private static final long MESSAGE_LIFETIME = 6 * 60 * 60 * 1000L; // 6 horas em milissegundos
    private static Set<PrintWriter> clientWriters = Collections.synchronizedSet(new HashSet<>());
    private static ConcurrentMap<Long, String> messageStore = new ConcurrentHashMap<>();
    private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Chat server started on port " + PORT);
            scheduler.scheduleAtFixedRate(chatServer::cleanupOldMessages, 0, 1, TimeUnit.HOURS);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void cleanupOldMessages() {
        long currentTime = System.currentTimeMillis();
        messageStore.entrySet().removeIf(entry -> currentTime - entry.getKey() > MESSAGE_LIFETIME);
        System.out.println("Cleaned up old messages.");
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                synchronized (clientWriters) {
                    clientWriters.add(out);
                    System.out.println("Added client writer. Total clients: " + clientWriters.size());
                }

                String message;
                while ((message = in.readLine()) != null) {
                    long timestamp = System.currentTimeMillis();
                    messageStore.put(timestamp, message);
                    System.out.println("Received message: " + message);
                    synchronized (clientWriters) {
                        for (PrintWriter writer : clientWriters) {
                            writer.println(message);
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Error handling client: " + e.getMessage());
                e.printStackTrace();
            } finally {
                try {
                    if (in != null) in.close();
                    if (out != null) out.close();
                    socket.close();
                } catch (IOException e) {
                    System.err.println("Error closing resources: " + e.getMessage());
                    e.printStackTrace();
                }
                synchronized (clientWriters) {
                    clientWriters.remove(out);
                    System.out.println("Removed client writer. Total clients: " + clientWriters.size());
                }
            }
        }
    }
}
