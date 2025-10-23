package org.example.appbanquee;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction {
    private LocalDateTime date;
    private String typeOperation;
    private double montant;
    private double soldeFinal;
    private String numeroCompte;
    
    public Transaction(LocalDateTime date, String typeOperation, double montant, double soldeFinal, String numeroCompte) {
        this.date = date;
        this.typeOperation = typeOperation;
        this.montant = montant;
        this.soldeFinal = soldeFinal;
        this.numeroCompte = numeroCompte;
    }
    
    public LocalDateTime getDate() {
        return date;
    }
    
    public String getDateFormatted() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return date.format(formatter);
    }
    
    public String getTypeOperation() {
        return typeOperation;
    }
    
    public double getMontant() {
        return montant;
    }
    
    public double getSoldeFinal() {
        return soldeFinal;
    }
    
    public String getNumeroCompte() {
        return numeroCompte;
    }
}