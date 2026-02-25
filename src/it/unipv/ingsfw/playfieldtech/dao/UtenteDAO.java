package it.unipv.ingsfw.playfieldtech.dao;

import it.unipv.ingsfw.playfieldtech.db.DatabaseManager;
import it.unipv.ingsfw.playfieldtech.exceptions.DatiConflittoException;
import it.unipv.ingsfw.playfieldtech.exceptions.DataAccessException;
import it.unipv.ingsfw.playfieldtech.exceptions.RisorsaNonTrovataException;
import it.unipv.ingsfw.playfieldtech.factory.UtenteFactory;
import it.unipv.ingsfw.playfieldtech.model.Cliente;
import it.unipv.ingsfw.playfieldtech.model.Utente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/*
* DAO specializzato per la gestione degli utenti. Si occupa di accedere al database per validare le credenziali di login,
* per salvare nuovi utenti e per recuperare un utente in base al suo username.
*/
public class UtenteDAO {
    private Connection connection;
    private UtenteFactory utenteFactory;
    
    public UtenteDAO() {
        this.connection = DatabaseManager.getInstance().getConnection();
        this.utenteFactory = new UtenteFactory();
    }

    public Utente validaLogin(String username, String password) throws RisorsaNonTrovataException, DataAccessException {
        String query = "SELECT * FROM Utenti WHERE username = ? AND password = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password); 
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id_utente");
                    String nome = rs.getString("nome");
                    String cognome = rs.getString("cognome");
                    String ruolo = rs.getString("ruolo");
                    return utenteFactory.creaUtente(ruolo, id, username, password, nome, cognome);
                } else {
                    throw new RisorsaNonTrovataException("Credenziali non valide.");
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Errore durante la validazione del login.", e);
        }
    }

    public void salvaUtente(Utente utente) throws DatiConflittoException, DataAccessException {
        String query = "INSERT INTO Utenti (username, password, nome, cognome, ruolo) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, utente.getUsername());
            stmt.setString(2, utente.getPassword());
            stmt.setString(3, utente.getNome());
            stmt.setString(4, utente.getCognome());
            String ruolo = (utente instanceof Cliente) ? "cliente" : "amministratore";
            stmt.setString(5, ruolo);
            stmt.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new DatiConflittoException("L'username '" + utente.getUsername() + "' è già in uso.");
            } else { 
                throw new DataAccessException("Errore DB durante il salvataggio dell'utente.", e);
            }
        }
    }

    public Utente findUtenteByUsername(String username) throws RisorsaNonTrovataException, DataAccessException {
        String query = "SELECT * FROM Utenti WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id_utente");
                    String nome = rs.getString("nome");
                    String cognome = rs.getString("cognome");
                    String ruolo = rs.getString("ruolo");
                    String password = rs.getString("password");
                    return utenteFactory.creaUtente(ruolo, id, username, password, nome, cognome);
                } else {
                    throw new RisorsaNonTrovataException("Nessun utente trovato con username: " + username);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Errore durante la ricerca dell'utente.", e);
        }
    }
}