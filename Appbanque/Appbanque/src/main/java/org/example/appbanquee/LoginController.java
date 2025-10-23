package org.example.appbanquee;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {
    private static final String URL = "jdbc:sqlite:table des comptes.db";
    
    @FXML
    private TextField numeroCompteField;
    
    @FXML
    private TextField nomField;
    
    @FXML
    private Label messageLabel;
    
    @FXML
    protected void onLoginButtonClick() {
        String numeroCompte = numeroCompteField.getText().trim();
        String nom = nomField.getText().trim();
        
        if (numeroCompte.isEmpty() || nom.isEmpty()) {
            messageLabel.setText("Veuillez remplir tous les champs");
            return;
        }
        
        try (Connection connexion = DriverManager.getConnection(URL);
             PreparedStatement stmt = connexion.prepareStatement(
                     "SELECT * FROM Comptes WHERE numerocompte = ? AND nom = ?")) {
            
            stmt.setString(1, numeroCompte);
            stmt.setString(2, nom);
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                // Connexion réussie, ouvrir l'écran principal
                ouvrirEcranPrincipal(numeroCompte);
            } else {
                messageLabel.setText("Numéro de compte ou nom incorrect");
            }
            
        } catch (SQLException e) {
            messageLabel.setText("Erreur de connexion à la base de données");
            e.printStackTrace();
        } catch (IOException e) {
            messageLabel.setText("Erreur lors de l'ouverture de l'application");
            e.printStackTrace();
        }
    }
    
    @FXML
    protected void onCreateAccountButtonClick() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("create-account-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 600, 400);
            Stage stage = (Stage) numeroCompteField.getScene().getWindow();
            stage.setTitle("Création de compte");
            stage.setScene(scene);
        } catch (IOException e) {
            messageLabel.setText("Erreur lors de l'ouverture du formulaire de création");
            e.printStackTrace();
        }
    }
    
    private void ouvrirEcranPrincipal(String numeroCompte) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        
        MainController controller = fxmlLoader.getController();
        controller.initialiserCompte(numeroCompte);
        
        Stage stage = (Stage) numeroCompteField.getScene().getWindow();
        stage.setTitle("Application Bancaire");
        stage.setScene(scene);
    }
}