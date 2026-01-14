package fr.insa.mas.Controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class DatabaseLogger {

    // URL de la base de données
    private static final String DB_URL = "jdbc:mysql://localhost:3306/projet_gei_011";

    // Nom d'utilisateur de la base
    private static final String DB_USER = "projet_gei_011";

    // Mot de passe de la base
    private static final String DB_PASSWORD = "Soo2aang";

    /**
     * Méthode pour enregistrer un événement dans la table `events`.
     *
     * @param eventDescription Description de l'événement à enregistrer.
     */
    public void logEvent(String eventDescription) {
        System.out.println("logEvent called with description: " + eventDescription);

        // Requête SQL pour insérer un événement
        String insertQuery = "INSERT INTO events (event_description, event_date) VALUES (?, ?)";
        LocalDateTime now = LocalDateTime.now();
        String formattedDate = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        try (
            // Établit une connexion avec la base de données
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            // Prépare la requête SQL
            PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)
        ) {
            System.out.println("Connection established successfully.");

            // Paramètre les valeurs de la requête
            preparedStatement.setString(1, eventDescription);
            preparedStatement.setString(2, formattedDate);

            // Exécute la requête et vérifie si une ligne a été insérée
            int rowsInserted = preparedStatement.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Event logged successfully: " + eventDescription + " at " + formattedDate);
            } else {
                System.out.println("Failed to log event: " + eventDescription);
            }

        } catch (SQLException e) {
            // Affiche les détails de l'exception en cas d'erreur SQL
            System.err.println("Error logging event: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
