package ec.edu.espe.buildtestci.model;

import java.util.UUID;

public class Wallet {

    private final String id;
    private final String ownerEmail;
    private double balance;

    public Wallet(String ownerEmail, double balance) {
        this.id = UUID.randomUUID().toString();
        this.ownerEmail = ownerEmail;
        this.balance = balance;
    }

    public String getId() {
        return id;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    //Depositar dinero en la cuentilla
    public void deposit(double amount){
        this.balance+= amount;
    }

    //Retirar dinero si existe saldo suficiente
    public void withdraw(double amount){
        this.balance -= amount;
    }
}
