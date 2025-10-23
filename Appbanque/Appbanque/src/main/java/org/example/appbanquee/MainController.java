package org.example.appbanquee;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    private static final String URL = "jdbc:sqlite:table des comptes.db";
    
    private String numeroCompte;
    private String typeCompte;
    private double solde;
    private String nom;
    
    @FXML
    private Label infoCompteLabel;
    
    @FXML
    private TextField montantField;
    
    @FXML
    private Label messageOperationLabel;
    
    @FXML
    private VBox compteEpargneControls;
    
    @FXML
    private Label tauxInteretLabel;
    
    @FXML
    private TableView<Transaction> historiqueTableView;
    
    @FXML
    private TableColumn<Transaction, String> dateColonne;
    
    @FXML
    private TableColumn<Transaction, String> operationColonne;
    
    @FXML
    private TableColumn<Transaction, Double> montantColonne;
    
    @FXML
    private TableColumn<Transaction, Double> soldeColonne;
    
    private ObservableList<Transaction> transactionsList = FXCollections.observableArrayList();
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialiser les colonnes du tableau d'historique
        dateColonne.setCellValueFactory(new PropertyValueFactory<>("dateFormatted"));
        operationColonne.setCellValueFactory(new PropertyValueFactory<>("typeOperation"));
        montantColonne.setCellValueFactory(new PropertyValueFactory<>("montant"));
        soldeColonne.setCellValueFactory(new PropertyValueFactory<>("soldeFinal"));
        
        historiqueTableView.setItems(transactionsList);
    }
    
    public void initialiserCompte(String numeroCompte) {
        this.numeroCompte = numeroCompte;
        chargerInfosCompte();
        chargerHistoriqueTransactions();
    }
    
    private void chargerInfosCompte() {
        try (Connection connexion = DriverManager.getConnection(URL)) {
            // Récupérer les informations de base du compte
            try (PreparedStatement stmt = connexion.prepareStatement(
                    "SELECT * FROM Comptes WHERE numerocompte = ?")) {
                stmt.setString(1, numeroCompte);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    this.nom = rs.getString("nom");
                    this.solde = rs.getDouble("solde");
                    
                    // Vérifier si c'est un compte courant
                    try (PreparedStatement stmtCourant = connexion.prepareStatement(
                            "SELECT * FROM ComptesCourants WHERE numerocompte_ref = ?")) {
                        stmtCourant.setString(1, numeroCompte);
                        ResultSet rsCourant = stmtCourant.executeQuery();
                        
                        if (rsCourant.next()) {
                            this.typeCompte = "Courant";
                            double decouvert = rsCourant.getDouble("decouvertautorise");
                            infoCompteLabel.setText(String.format(
                                    "Compte Courant - N°: %s | Titulaire: %s | Solde: %.2f€ | Découvert autorisé: %.2f€",
                                    numeroCompte, nom, solde, decouvert));
                            compteEpargneControls.setVisible(false);
                            return;
                        }
                    }
                    
                    // Vérifier si c'est un compte épargne
                    try (PreparedStatement stmtEpargne = connexion.prepareStatement(
                            "SELECT * FROM ComptesEpargnes WHERE numerocompte_ref = ?")) {
                        stmtEpargne.setString(1, numeroCompte);
                        ResultSet rsEpargne = stmtEpargne.executeQuery();
                        
                        if (rsEpargne.next()) {
                            this.typeCompte = "Epargne";
                            double tauxInteret = rsEpargne.getDouble("tauxinteret");
                            infoCompteLabel.setText(String.format(
                                    "Compte Épargne - N°: %s | Titulaire: %s | Solde: %.2f€",
                                    numeroCompte, nom, solde));
                            tauxInteretLabel.setText(String.format("Taux d'intérêt: %.2f%%", tauxInteret * 100));
                            compteEpargneControls.setVisible(true);
                            return;
                        }
                    }
                }
            }
            
            // Si on arrive ici, c'est qu'il y a un problème avec le compte
            infoCompteLabel.setText("Erreur: Impossible de charger les informations du compte");
            
        } catch (SQLException e) {
            infoCompteLabel.setText("Erreur de connexion à la base de données");
            e.printStackTrace();
        }
    }
    
    @FXML
    protected void onDepotButtonClick() {
        try {
            double montant = Double.parseDouble(montantField.getText());
            if (montant <= 0) {
                messageOperationLabel.setText("Le montant doit être positif");
                return;
            }
            
            try (Connection connexion = DriverManager.getConnection(URL);
                 PreparedStatement stmt = connexion.prepareStatement(
                         "UPDATE Comptes SET solde = solde + ? WHERE numerocompte = ?")) {
                
                stmt.setDouble(1, montant);
                stmt.setString(2, numeroCompte);
                
                int lignesModifiees = stmt.executeUpdate();
                if (lignesModifiees > 0) {
                    solde += montant;
                    messageOperationLabel.setText(String.format("Dépôt de %.2f€ effectué avec succès", montant));
                    // Enregistrer la transaction
                    enregistrerTransaction("Dépôt", montant, solde);
                    chargerInfosCompte(); // Rafraîchir les informations affichées
                    chargerHistoriqueTransactions(); // Rafraîchir l'historique
                } else {
                    messageOperationLabel.setText("Erreur lors du dépôt");
                }
            }
        } catch (NumberFormatException e) {
            messageOperationLabel.setText("Veuillez entrer un montant valide");
        } catch (SQLException e) {
            messageOperationLabel.setText("Erreur de connexion à la base de données");
            e.printStackTrace();
        }
    }
    
    @FXML
    protected void onRetraitButtonClick() {
        try {
            double montant = Double.parseDouble(montantField.getText());
            if (montant <= 0) {
                messageOperationLabel.setText("Le montant doit être positif");
                return;
            }
            
            // Vérifier si le retrait est possible selon le type de compte
            if (typeCompte.equals("Epargne") && montant > solde) {
                messageOperationLabel.setText("Solde insuffisant pour un compte épargne");
                return;
            } else if (typeCompte.equals("Courant")) {
                // Vérifier le découvert autorisé pour un compte courant
                try (Connection connexion = DriverManager.getConnection(URL);
                     PreparedStatement stmt = connexion.prepareStatement(
                             "SELECT decouvertautorise FROM ComptesCourants WHERE numerocompte_ref = ?")) {
                    
                    stmt.setString(1, numeroCompte);
                    ResultSet rs = stmt.executeQuery();
                    
                    if (rs.next()) {
                        double decouvert = rs.getDouble("decouvertautorise");
                        if (solde - montant < -decouvert) {
                            messageOperationLabel.setText("Dépassement du découvert autorisé");
                            return;
                        }
                    }
                }
            }
            
            // Effectuer le retrait
            try (Connection connexion = DriverManager.getConnection(URL);
                 PreparedStatement stmt = connexion.prepareStatement(
                         "UPDATE Comptes SET solde = solde - ? WHERE numerocompte = ?")) {
                
                stmt.setDouble(1, montant);
                stmt.setString(2, numeroCompte);
                
                int lignesModifiees = stmt.executeUpdate();
                if (lignesModifiees > 0) {
                    solde -= montant;
                    messageOperationLabel.setText(String.format("Retrait de %.2f€ effectué avec succès", montant));
                    // Enregistrer la transaction
                    enregistrerTransaction("Retrait", -montant, solde);
                    chargerInfosCompte(); // Rafraîchir les informations affichées
                    chargerHistoriqueTransactions(); // Rafraîchir l'historique
                } else {
                    messageOperationLabel.setText("Erreur lors du retrait");
                }
            }
        } catch (NumberFormatException e) {
            messageOperationLabel.setText("Veuillez entrer un montant valide");
        } catch (SQLException e) {
            messageOperationLabel.setText("Erreur de connexion à la base de données");
            e.printStackTrace();
        }
    }
    
    @FXML
    protected void onAppliquerInteretsButtonClick() {
        if (!typeCompte.equals("Epargne")) {
            return;
        }
        
        try (Connection connexion = DriverManager.getConnection(URL)) {
            // Récupérer le taux d'intérêt
            double tauxInteret;
            try (PreparedStatement stmt = connexion.prepareStatement(
                    "SELECT tauxinteret FROM ComptesEpargnes WHERE numerocompte_ref = ?")) {
                
                stmt.setString(1, numeroCompte);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    tauxInteret = rs.getDouble("tauxinteret");
                } else {
                    messageOperationLabel.setText("Erreur: Impossible de récupérer le taux d'intérêt");
                    return;
                }
            }
            
            // Calculer et appliquer les intérêts
            double interets = solde * tauxInteret;
            try (PreparedStatement stmt = connexion.prepareStatement(
                    "UPDATE Comptes SET solde = solde + ? WHERE numerocompte = ?")) {
                
                stmt.setDouble(1, interets);
                stmt.setString(2, numeroCompte);
                
                int lignesModifiees = stmt.executeUpdate();
                if (lignesModifiees > 0) {
                    solde += interets;
                    messageOperationLabel.setText(String.format(
                            "Intérêts de %.2f€ appliqués avec succès (taux: %.2f%%)",
                            interets, tauxInteret * 100));
                    // Enregistrer la transaction
                    enregistrerTransaction("Intérêts", interets, solde);
                    chargerInfosCompte(); // Rafraîchir les informations affichées
                    chargerHistoriqueTransactions(); // Rafraîchir l'historique
                } else {
                    messageOperationLabel.setText("Erreur lors de l'application des intérêts");
                }
            }
        } catch (SQLException e) {
            messageOperationLabel.setText("Erreur de connexion à la base de données");
            e.printStackTrace();
        }
    }
    
    @FXML
    protected void onDeconnexionButtonClick() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        Stage stage = (Stage) infoCompteLabel.getScene().getWindow();
        stage.setTitle("Connexion - Application Bancaire");
        stage.setScene(scene);
    }
    
    private void enregistrerTransaction(String typeOperation, double montant, double soldeFinal) {
        try (Connection connexion = DriverManager.getConnection(URL);
             PreparedStatement stmt = connexion.prepareStatement(
                     "CREATE TABLE IF NOT EXISTS Transactions ("
                     + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                     + "date TEXT, "
                     + "type_operation TEXT, "
                     + "montant REAL, "
                     + "solde_final REAL, "
                     + "numerocompte_ref TEXT, "
                     + "FOREIGN KEY (numerocompte_ref) REFERENCES Comptes(numerocompte))")) {
            
            stmt.executeUpdate();
            
            // Insérer la nouvelle transaction
            try (PreparedStatement insertStmt = connexion.prepareStatement(
                    "INSERT INTO Transactions (date, type_operation, montant, solde_final, numerocompte_ref) "
                    + "VALUES (?, ?, ?, ?, ?)")) {
                
                LocalDateTime now = LocalDateTime.now();
                insertStmt.setString(1, now.toString());
                insertStmt.setString(2, typeOperation);
                insertStmt.setDouble(3, montant);
                insertStmt.setDouble(4, soldeFinal);
                insertStmt.setString(5, numeroCompte);
                
                insertStmt.executeUpdate();
            }
            
        } catch (SQLException e) {
            messageOperationLabel.setText("Erreur lors de l'enregistrement de la transaction");
            e.printStackTrace();
        }
    }
    
    private void chargerHistoriqueTransactions() {
        transactionsList.clear();
        
        try (Connection connexion = DriverManager.getConnection(URL)) {
            // Vérifier si la table existe
            try (PreparedStatement checkStmt = connexion.prepareStatement(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name='Transactions'")) {
                
                ResultSet rs = checkStmt.executeQuery();
                if (!rs.next()) {
                    // La table n'existe pas encore
                    return;
                }
            }
            
            // Récupérer les transactions pour ce compte
            try (PreparedStatement stmt = connexion.prepareStatement(
                    "SELECT * FROM Transactions WHERE numerocompte_ref = ? ORDER BY date DESC")) {
                
                stmt.setString(1, numeroCompte);
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    LocalDateTime date = LocalDateTime.parse(rs.getString("date"));
                    String typeOperation = rs.getString("type_operation");
                    double montant = rs.getDouble("montant");
                    double soldeFinal = rs.getDouble("solde_final");
                    
                    Transaction transaction = new Transaction(date, typeOperation, montant, soldeFinal, numeroCompte);
                    transactionsList.add(transaction);
                }
            }
            
        } catch (SQLException e) {
            messageOperationLabel.setText("Erreur lors du chargement de l'historique");
            e.printStackTrace();
        }
    }
}