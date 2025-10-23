package org.example.appbanquee;

public class compteepargne extends compte {
    private double tauxinteret;

    public compteepargne(  String nom, String numerocomptee, double soldeinitial, double tauxinteret) {
        super(nom,numerocomptee,soldeinitial );
        this.tauxinteret = tauxinteret;
    }

    // Getter
    public double gettauxinteret() {
        return tauxinteret;
    }

    // Setter
    public void settauxinteret(double tauxinteret) {
        this.tauxinteret = tauxinteret;
    }

    public void appliquerinteret() {
        double interets = getsolde() * tauxinteret;
        setsolde(getsolde() + interets);
        System.out.println("intérêts sont: " + interets);
    }

    @Override
    public void retirer(double montant) {
        if (getsolde() >= montant) {
            setsolde(getsolde() - montant);
            System.out.println("retrait de" + montant );
        } else {
            System.out.println("retrait refusé");
        }
    }

    @Override
    public void afficherdetails() {
        super.afficherdetails();
        System.out.println(" org.example.gestioncompte.HelloController.compte épargne");
        System.out.println("taux intérêt: " + tauxinteret);
    }
}



