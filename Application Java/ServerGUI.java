package Projet;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class ServerGUI {

    // Liste pour stocker les flux de sortie des clients
    private static final List<PrintWriter> clientOutputs = new ArrayList<>();
    private static JTextArea messageArea;

    public static void main(String[] args) {
        final int port = 55203; // Changez le port si nécessaire

        // Création de l'interface graphique
        JFrame frame = new JFrame("Serveur de Chat");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);

        messageArea = new JTextArea();
        messageArea.setEditable(false); // Zone de texte non modifiable
        frame.add(new JScrollPane(messageArea));
        frame.setVisible(true);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            appendMessage("Serveur en attente de connexions sur le port " + port + "\n");

            while (true) {
                // Accepter les connexions des clients
                Socket clientSocket = serverSocket.accept();
                appendMessage("Nouveau client connecté : " + clientSocket.getInetAddress() + "\n");

                // Gérer chaque client dans un thread
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            appendMessage("Erreur : " + e.getMessage() + "\n");
        }
    }

    // Ajouter des messages dans la zone de texte en toute sécurité
    private static void appendMessage(String message) {
        SwingUtilities.invokeLater(() -> messageArea.append(message));
    }

    // Classe interne pour gérer les connexions des clients
    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private PrintWriter writer;
        private BufferedReader reader;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try {
                // Initialiser les flux de communication
                reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                writer = new PrintWriter(clientSocket.getOutputStream(), true);

                // Ajouter le client à la liste
                synchronized (clientOutputs) {
                    clientOutputs.add(writer);
                }

                // Lecture des messages du client
                String msg;
                while ((msg = reader.readLine()) != null) {
                    appendMessage("Message reçu : " + msg + "\n");
                    broadcastMessage(msg);
                }
            } catch (IOException e) {
                appendMessage("Erreur avec un client : " + e.getMessage() + "\n");
            } finally {
                // Supprimer le client de la liste à la déconnexion
                synchronized (clientOutputs) {
                    clientOutputs.remove(writer);
                }
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                appendMessage("Client déconnecté.\n");
            }
        }

        // Méthode pour envoyer un message à tous les clients
        private void broadcastMessage(String message) {
            synchronized (clientOutputs) {
                for (PrintWriter pw : clientOutputs) {
                    pw.println(message);
                }
            }
        }
    }
}
