package Projet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client2 {
    public static void main(String[] args) {
        final String host = "192.168.28.189"; // AdresseIPduserveur
        final int port = 55203;

        try (Socket clientSocket = new Socket(host, port)) {
            System.out.println("Connecté au serveur");

            PrintWriter PW = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader BR = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            Scanner scanner = new Scanner(System.in);

            // Demander un nom d'utilisateur
            
            System.out.print("Entrer nom : ");
            String username = scanner.nextLine();
            PW.println(username + " a rejoint la conversation !");

            // Thread pour envoyer des messages au serveur
            Thread sender = new Thread(() -> {
                try {
                    while (true) {
                        String msg = scanner.nextLine();
                        if (msg.equalsIgnoreCase("exit")) {
                            PW.println(username + " a quitté la conversation.");
                            clientSocket.close(); // Déconnexion propre
                            break;
                        }
                        PW.println(username + ": " + msg);
                    }
                } catch (IOException e) {
                    System.out.println("Connexion au serveur perdue.");
                }
            });
            sender.start();

            // Thread pour recevoir des messages du serveur
            Thread receiver = new Thread(() -> {
                try {
                    String msg;
                    while ((msg = BR.readLine()) != null) {
                        System.out.println(msg);
                    }
                } catch (IOException e) {
                    System.out.println("Connexion au serveur terminée.");
                }
            });
            receiver.start();

            // Attendre que le thread sender se termine
            sender.join();

        } catch (IOException | InterruptedException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }
}