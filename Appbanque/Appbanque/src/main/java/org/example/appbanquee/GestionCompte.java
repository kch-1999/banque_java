package org.example.appbanquee;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.Scanner;


public class GestionCompte {
    private static final String URL = "jdbc:sqlite:table des comptes.db";

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        try {
            Connection connexion = DriverManager.getConnection(URL);
            Statement st = connexion.createStatement();

            st.execute("CREATE TABLE IF NOT EXISTS Comptes (" +
                    "numerocompte TEXT PRIMARY KEY," +
                    "nom TEXT," +
                    "solde REAL" +
                    ")");

            st.execute("CREATE TABLE IF NOT EXISTS ComptesCourants (" +
                    "numerocompte_ref TEXT PRIMARY KEY," +
                    "decouvertautorise REAL," +
                    "FOREIGN KEY(numerocompte_ref) REFERENCES Comptes(numerocompte)" +
                    ")");

            st.execute("CREATE TABLE IF NOT EXISTS ComptesEpargnes (" +
                    "numerocompte_ref TEXT PRIMARY KEY," +
                    "tauxinteret REAL," +
                    "FOREIGN KEY(numerocompte_ref) REFERENCES Comptes(numerocompte)" +
                    ")");

            System.out.print("Type de compte (C pour Courant, E pour Epargne) : ");
            String typeCompte = sc.nextLine().toUpperCase();

            System.out.print("Numéro de compte : ");
            String numerocompte = sc.nextLine();

            System.out.print("Nom du titulaire : ");
            String nom = sc.nextLine();

            System.out.print("Solde initial : ");
            double solde = sc.nextDouble();
            sc.nextLine();

            st.executeUpdate("INSERT INTO Comptes (numerocompte, nom, solde) VALUES ('" +
                    numerocompte + "', '" + nom + "', " + solde + ")");

            if (typeCompte.equals("C")) {
                System.out.print("Découvert autorisé : ");
                double decouvert = sc.nextDouble();
                sc.nextLine();

                st.executeUpdate("INSERT INTO ComptesCourants (numerocompte_ref, decouvertautorise) VALUES ('" +
                        numerocompte + "', " + decouvert + ")");

                System.out.println("Compte courant créé avec succès");
            } else if (typeCompte.equals("E")) {
                System.out.print("Taux d'intérêt : ");
                double taux = sc.nextDouble();
                sc.nextLine();

                st.executeUpdate("INSERT INTO ComptesEpargnes (numerocompte_ref, tauxinteret) VALUES ('" +
                        numerocompte + "', " + taux + ")");

                System.out.println("Compte épargne créé avec succès");
            }

            st.close();
            connexion.close();
            sc.close();

        } catch (SQLException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }

    public static boolean verifierNumeroCompte(String numerocompte) {
        try (Connection connexion = DriverManager.getConnection(URL)) {
            Statement st = connexion.createStatement();
            String sql = "SELECT numerocompte FROM Comptes WHERE numerocompte = '" + numerocompte + "'";
            ResultSet rs = st.executeQuery(sql);
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }

    public static void deposer(String numerocompte, double montant) {
        try (Connection connexion = DriverManager.getConnection(URL)) {
            Statement st = connexion.createStatement();
            String sql = "UPDATE Comptes SET solde = solde + " + montant +
                    " WHERE numerocompte = '" + numerocompte + "'";
            st.executeUpdate(sql);
        } catch (SQLException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }

    public static void retirer(String numerocompte, double montant) {
        try (Connection connexion = DriverManager.getConnection(URL)) {
            Statement st = connexion.createStatement();

            String requete = "SELECT c.solde, " +
                    "CASE WHEN cc.decouvertautorise IS NOT NULL THEN 'COURANT' ELSE 'EPARGNE' END as type, " +
                    "COALESCE(cc.decouvertautorise, 0) as decouvert " +
                    "FROM Comptes c " +
                    "LEFT JOIN ComptesCourants cc ON c.numerocompte = cc.numerocompte_ref " +
                    "WHERE c.numerocompte = '" + numerocompte + "'";

            ResultSet rs = st.executeQuery(requete);

            if (rs.next()) {
                double solde = rs.getDouble("solde");
                String type = rs.getString("type");
                double decouvert = rs.getDouble("decouvert");

                boolean retraitPossible = type.equals("COURANT") ?
                        (solde - montant) >= -decouvert : solde >= montant;

                if (retraitPossible) {
                    String sql = "UPDATE Comptes SET solde = solde - " + montant +
                            " WHERE numerocompte = '" + numerocompte + "'";
                    st.executeUpdate(sql);
                }
            }
        } catch (SQLException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }
}


