package it.unipv.ingsfw.playfieldtech.model;

// Classe che rappresenta un amministratore, che estende Utente e può gestire il sistema
public class Amministratore extends Utente {
    public Amministratore(int idUtente, String username, String password, String nome, String cognome) {
        super(idUtente, username, password, nome, cognome);
    }
}