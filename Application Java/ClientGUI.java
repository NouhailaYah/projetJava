package Projet;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientGUI {
    private JFrame frame;
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private JTextField usernameField;
    private PrintWriter writer;
    private BufferedReader reader;
    private Socket clientSocket;

    public ClientGUI(String host, int port) {
        try {
            // Connexion au serveur
            clientSocket = new Socket(host, port);
            writer = new PrintWriter(clientSocket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            // Configuration de l'interface graphique
            setupGUI();

            // Démarrage du thread de réception
            new Thread(this::receiveMessages).start();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Impossible de se connecter au serveur", "Erreur", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void setupGUI() {
        // Création de la fenêtre principale
        frame = new JFrame("Client de Chat");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);

        // Zone de discussion
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(chatArea);

        // Champ pour entrer le nom d'utilisateur
        JPanel usernamePanel = new JPanel();
        usernamePanel.setLayout(new BorderLayout());
        usernameField = new JTextField("Entrez votre nom d'utilisateur");
        usernamePanel.add(usernameField, BorderLayout.CENTER);

        // Champ pour écrire des messages
        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new BorderLayout());
        messageField = new JTextField();
        sendButton = new JButton("Envoyer");

        // Ajouter des actions
        sendButton.addActionListener(e -> sendMessage());

        // Ajouter les composants au panneau
        messagePanel.add(messageField, BorderLayout.CENTER);
        messagePanel.add(sendButton, BorderLayout.EAST);

        // Ajout des composants à la fenêtre
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        frame.getContentPane().add(usernamePanel, BorderLayout.NORTH);
        frame.getContentPane().add(messagePanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private void sendMessage() {
        String username = usernameField.getText().trim();
        String message = messageField.getText().trim();

        if (!username.isEmpty() && !message.isEmpty()) {
            writer.println(username + ": " + message);
            messageField.setText("");
        }
    }

    private void receiveMessages() {
        try {
            String message;
            while ((message = reader.readLine()) != null) {
                chatArea.append(message + "\n");
            }
        } catch (IOException e) {
            chatArea.append("Connexion au serveur perdue.\n");
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClientGUI("192.168.28.189", 55203));
    }
}