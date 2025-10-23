package org.example.appbanquee;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class AppBancaireApplication extends Application {
    private static final String URL = "jdbc:sqlite:table des comptes.db";
    
    @Override
    public void start(Stage stage) throws IOException {
        // Initialiser la base de données
        initialiserBaseDeDonnees();
        
        // Charger l'écran de connexion
        FXMLLoader fxmlLoader = new FXMLLoader(AppBancaireApplication.class.getResource("login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        stage.setTitle("Connexion - Application Bancaire");
        stage.setScene(scene);
        stage.show();
    }
    
    private void initialiserBaseDeDonnees() {
        try (Connection connexion = DriverManager.getConnection(URL);
             Statement st = connexion.createStatement()) {
            
            // Créer la table des comptes si elle n'existe pas
            st.execute("CREATE TABLE IF NOT EXISTS Comptes (" +
                    "numerocompte TEXT PRIMARY KEY," +
                    "nom TEXT," +
                    "solde REAL" +
                    ")");
            
            // Créer la table des comptes courants si elle n'existe pas
            st.execute("CREATE TABLE IF NOT EXISTS ComptesCourants (" +
                    "numerocompte_ref TEXT PRIMARY KEY," +
                    "decouvertautorise REAL," +
                    "FOREIGN KEY(numerocompte_ref) REFERENCES Comptes(numerocompte)" +
                    ")");
            
            // Créer la table des comptes épargnes si elle n'existe pas
            st.execute("CREATE TABLE IF NOT EXISTS ComptesEpargnes (" +
                    "numerocompte_ref TEXT PRIMARY KEY," +
                    "tauxinteret REAL," +
                    "FOREIGN KEY(numerocompte_ref) REFERENCES Comptes(numerocompte)" +
                    ")");
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'initialisation de la base de données : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}