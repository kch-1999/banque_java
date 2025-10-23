package org.example.appbanquee;

public class comptecourant extends compte {
    private double decouvertautorise;

    public comptecourant(  String nom, String numerocomptee , double soldeinitial, double decouvertautorise) {
        super(nom, numerocomptee,soldeinitial);
        this.decouvertautorise = decouvertautorise;
    }


    public double getdecouvertautorise() {
        return decouvertautorise;
    }


    public void setdecouvertautorise(double decouvertautorise) {
        this.decouvertautorise = decouvertautorise;
    }

    @Override
    public void retirer(double montant) {
        if (getsolde() - montant >= -decouvertautorise) {
            setsolde(getsolde() - montant);
            System.out.println("retrait fait  " + montant );
        } else {
            System.out.println("retrait refusé ");
        }
    }

    @Override
    public void afficherdetails() {
        super.afficherdetails();
        System.out.println("org.example.gestioncompte.HelloController.compte courant");
        System.out.println("découvert autorisé: " + decouvertautorise);
    }
}

