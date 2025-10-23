package org.example.appbanquee;
import java.util.ArrayList;
import java.util.List;

public abstract class compte implements operations {
    private String numerocompte;
    private double solde;
    private String nom;

    // Collection statique pour stocker tous les comptes
    private static List<compte> listeComptes = new ArrayList<>();

    public compte(String nom, String numerocompte, double solde) {
        this.nom = nom;
        this.numerocompte = numerocompte;
        this.solde = solde;
    }

    public String getnumerocompte() {
        return numerocompte;
    }

    public void setnumerocompte(String numerocompte) {
        this.numerocompte = numerocompte;
    }

    public double getsolde() {
        return solde;
    }

    public void setsolde(double solde) {
        this.solde = solde;
    }

    public String getnom() {
        return nom;
    }

    public void setnom(String nom) {
        this.nom = nom;
    }

    @Override
    public void deposer(double montant) {
        if (montant > 0) {
            solde += montant;
            System.out.println("Dépôt de " + montant + " effectué avec succès");
        } else {
            System.out.println("Le montant du dépôt doit être positif");
        }
    }

    @Override
    public void retirer(double montant) {
        if (montant > 0) {
            if (solde >= montant) {
                solde -= montant;
                System.out.println("Retrait de " + montant + " effectué avec succès");
            } else {
                System.out.println("Solde insuffisant pour effectuer ce retrait");
            }
        } else {
            System.out.println("Le montant du retrait doit être positif");
        }
    }

    public void afficherdetails() {
        System.out.println("Numéro de compte: " + numerocompte);
        System.out.println("Nom du titulaire: " + nom);
        System.out.println("Solde: " + solde);
    }

    // Méthodes pour gérer la collection
    public static void ajouterCompte(compte c) {
        if (c != null) {
            listeComptes.add(c);
        }
    }

    public static compte trouverCompte(String numerocompte) {
        for (compte c : listeComptes) {
            if (c.getnumerocompte().equals(numerocompte)) {
                return c;
            }
        }
        return null;
    }

    public static List<compte> getTousComptes() {
        return listeComptes;
    }

    public static List<comptecourant> getComptesCourants() {
        List<comptecourant> comptesCourants = new ArrayList<>();
        for (compte c : listeComptes) {
            if (c instanceof comptecourant) {
                comptesCourants.add((comptecourant) c);
            }
        }
        return comptesCourants;
    }

    public static List<compteepargne> getComptesEpargnes() {
        List<compteepargne> comptesEpargnes = new ArrayList<>();
        for (compte c : listeComptes) {
            if (c instanceof compteepargne) {
                comptesEpargnes.add((compteepargne) c);
            }
        }
        return comptesEpargnes;
    }

    public static void afficherTousLesComptes() {
        if (listeComptes.isEmpty()) {
            System.out.println("Aucun compte dans la collection");
            return;
        }
        for (compte c : listeComptes) {
            c.afficherdetails();
            System.out.println("------------------------");
        }
    }
}