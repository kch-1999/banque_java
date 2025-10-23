package org.example.appbanquee;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CreateAccountController {
    private static final String URL = "jdbc:sqlite:table des comptes.db";
    
    @FXML
    private TextField nomField;
    
    @FXML
    private TextField numeroCompteField;
    
    @FXML
    private TextField soldeInitialField;
    
    @FXML
    private RadioButton compteCourantRadio;
    
    @FXML
    private RadioButton compteEpargneRadio;
    
    @FXML
    private Label decouvertLabel;
    
    @FXML
    private TextField decouvertField;
    
    @FXML
    private Label tauxInteretLabel;
    
    @FXML
    private TextField tauxInteretField;
    
    @FXML
    private Label messageLabel;
    
    @FXML
    private ToggleGroup typeCompteGroup;
    
    @FXML
    public void initialize() {
        // Afficher/masquer les champs spécifiques selon le type de compte sélectionné
        compteCourantRadio.selectedProperty().addListener((observable, oldValue, newValue) -> {
            decouvertLabel.setVisible(newValue);
            decouvertField.setVisible(newValue);
            tauxInteretLabel.setVisible(!newValue);
            tauxInteretField.setVisible(!newValue);
        });
        
        compteEpargneRadio.selectedProperty().addListener((observable, oldValue, newValue) -> {
            tauxInteretLabel.setVisible(newValue);
            tauxInteretField.setVisible(newValue);
            decouvertLabel.setVisible(!newValue);
            decouvertField.setVisible(!newValue);
        });
    }
    
    @FXML
    protected void onCreateButtonClick() {
        String nom = nomField.getText().trim();
        String numeroCompte = numeroCompteField.getText().trim();
        String soldeInitialText = soldeInitialField.getText().trim();
        
        // Validation des champs
        if (nom.isEmpty() || numeroCompte.isEmpty() || soldeInitialText.isEmpty()) {
            messageLabel.setText("Veuillez remplir tous les champs obligatoires");
            return;
        }
        
        double soldeInitial;
        try {
            soldeInitial = Double.parseDouble(soldeInitialText);
            if (soldeInitial < 0) {
                messageLabel.setText("Le solde initial doit être positif");
                return;
            }
        } catch (NumberFormatException e) {
            messageLabel.setText("Le solde initial doit être un nombre valide");
            return;
        }
        
        boolean isCompteCourant = compteCourantRadio.isSelected();
        
        // Validation des champs spécifiques selon le type de compte
        double decouvert = 0;
        double tauxInteret = 0;
        
        if (isCompteCourant) {
            try {
                decouvert = Double.parseDouble(decouvertField.getText().trim());
                if (decouvert < 0) {
                    messageLabel.setText("Le découvert autorisé doit être positif");
                    return;
                }
            } catch (NumberFormatException e) {
                messageLabel.setText("Le découvert autorisé doit être un nombre valide");
                return;
            }
        } else {
            try {
                tauxInteret = Double.parseDouble(tauxInteretField.getText().trim());
                if (tauxInteret <= 0 || tauxInteret > 1) {
                    messageLabel.setText("Le taux d'intérêt doit être entre 0 et 1 (ex: 0.05 pour 5%)");
                    return;
                }
            } catch (NumberFormatException e) {
                messageLabel.setText("Le taux d'intérêt doit être un nombre valide");
                return;
            }
        }
        
        // Création du compte dans la base de données
        try (Connection connexion = DriverManager.getConnection(URL)) {
            // Vérifier si le numéro de compte existe déjà
            try (PreparedStatement checkStmt = connexion.prepareStatement(
                    "SELECT COUNT(*) FROM Comptes WHERE numerocompte = ?")) {
                checkStmt.setString(1, numeroCompte);
                if (checkStmt.executeQuery().getInt(1) > 0) {
                    messageLabel.setText("Ce numéro de compte existe déjà");
                    return;
                }
            }
            
            // Insérer dans la table Comptes
            try (PreparedStatement insertCompteStmt = connexion.prepareStatement(
                    "INSERT INTO Comptes (numerocompte, nom, solde) VALUES (?, ?, ?)")) {
                insertCompteStmt.setString(1, numeroCompte);
                insertCompteStmt.setString(2, nom);
                insertCompteStmt.setDouble(3, soldeInitial);
                insertCompteStmt.executeUpdate();
            }
            
            // Insérer dans la table spécifique selon le type de compte
            if (isCompteCourant) {
                try (PreparedStatement insertCourantStmt = connexion.prepareStatement(
                        "INSERT INTO ComptesCourants (numerocompte_ref, decouvertautorise) VALUES (?, ?)")) {
                    insertCourantStmt.setString(1, numeroCompte);
                    insertCourantStmt.setDouble(2, decouvert);
                    insertCourantStmt.executeUpdate();
                }
            } else {
                try (PreparedStatement insertEpargneStmt = connexion.prepareStatement(
                        "INSERT INTO ComptesEpargnes (numerocompte_ref, tauxinteret) VALUES (?, ?)")) {
                    insertEpargneStmt.setString(1, numeroCompte);
                    insertEpargneStmt.setDouble(2, tauxInteret);
                    insertEpargneStmt.executeUpdate();
                }
            }
            
            // Compte créé avec succès, retourner à l'écran de connexion
            messageLabel.setText("Compte créé avec succès!");
            retournerEcranConnexion();
            
        } catch (SQLException e) {
            messageLabel.setText("Erreur lors de la création du compte: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    @FXML
    protected void onCancelButtonClick() {
        try {
            retournerEcranConnexion();
        } catch (IOException e) {
            messageLabel.setText("Erreur lors du retour à l'écran de connexion");
            e.printStackTrace();
        }
    }
    
    private void retournerEcranConnexion() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.setTitle("Connexion - Application Bancaire");
        stage.setScene(scene);
    }
}