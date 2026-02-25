package it.unipv.ingsfw.playfieldtech.model;

// Classe astratta che rappresenta un utente del sistema, con attributi comuni a tutti gli utenti (id, username, password, nome, cognome)
public abstract class Utente {
    protected int idUtente;
    protected String username;
    protected String password;
    protected String nome;
    protected String cognome;

    public Utente(int idUtente, String username, String password, String nome, String cognome) {
        this.idUtente = idUtente;
        this.username = username;
        this.password = password;
        this.nome = nome;
        this.cognome = cognome;
    }

    public int getIdUtente() { return idUtente; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getNome() { return nome; }
    public String getCognome() { return cognome; }
}