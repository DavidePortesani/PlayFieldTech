package it.unipv.ingsfw.playfieldtech.model;

// Classe che rappresenta un cliente, che estende Utente e può prenotare partite
public class Cliente extends Utente {
    public Cliente(int idUtente, String username, String password, String nome, String cognome) {
        super(idUtente, username, password, nome, cognome);        
    }
    
    @Override
    public String toString() {
    return nome + " " + cognome + " (" + username + ")";
    }
}