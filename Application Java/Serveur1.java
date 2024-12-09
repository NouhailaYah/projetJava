package Projet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Serveur1 {

    // Liste pour stocker les flux de sortie des clients connectés
    private static final List<PrintWriter> clientOutputs = new ArrayList<>();

    public static void main(String[] args) {
        final int port = 55203;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Serveur en attente de connexions sur le port " + port);

            // Thread pour lire les messages du serveur et les diffuser aux clients
            new Thread(() -> {
                Scanner scanner = new Scanner(System.in);
                while (true) {
                    String serverMessage = scanner.nextLine(); // Lire l'entrée du serveur
                    broadcastMessage("Serveur : " + serverMessage);
                }
            }).start();

            // Boucle principale pour accepter les connexions clients
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nouveau client connecté : " + clientSocket.getInetAddress());

                // Créer un thread pour gérer ce client
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Classe interne pour gérer chaque client
    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private BufferedReader reader;
        private PrintWriter writer;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try {
                // Initialiser les flux de communication
                reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                writer = new PrintWriter(clientSocket.getOutputStream(), true);

                // Ajouter ce client à la liste
                synchronized (clientOutputs) {
                    clientOutputs.add(writer);
                }

                String msg;
                // Lire les messages du client
                while ((msg = reader.readLine()) != null) {
                    System.out.println("Message reçu : " + msg);

                    // Diffuser le message à tous les autres clients
                    broadcastMessage(msg);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // Déconnexion du client
                System.out.println("Client déconnecté : " + clientSocket.getInetAddress());
                synchronized (clientOutputs) {
                    clientOutputs.remove(writer);
                }
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Méthode pour diffuser un message à tous les clients
    private static void broadcastMessage(String msg) {
        synchronized (clientOutputs) {
            for (PrintWriter pw : clientOutputs) {
                pw.println(msg);
            }
        }
    }
}