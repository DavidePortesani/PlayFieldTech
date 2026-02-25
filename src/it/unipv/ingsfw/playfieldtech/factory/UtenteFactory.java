package it.unipv.ingsfw.playfieldtech.factory;

import it.unipv.ingsfw.playfieldtech.model.Amministratore;
import it.unipv.ingsfw.playfieldtech.model.Cliente;
import it.unipv.ingsfw.playfieldtech.model.Utente;

public class UtenteFactory {
    public Utente creaUtente(String tipo, int id, String user, String pass, String nome, String cognome) {
        if ("cliente".equalsIgnoreCase(tipo)) {
            return new Cliente(id, user, pass, nome, cognome);
        } 
        else if ("amministratore".equalsIgnoreCase(tipo)) {
            return new Amministratore(id, user, pass, nome, cognome);
        }
        return null;
    }
}