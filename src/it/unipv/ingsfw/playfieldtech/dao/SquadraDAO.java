package it.unipv.ingsfw.playfieldtech.dao;

import it.unipv.ingsfw.playfieldtech.db.DatabaseManager;
import it.unipv.ingsfw.playfieldtech.exceptions.DatiConflittoException;
import it.unipv.ingsfw.playfieldtech.exceptions.DataAccessException;
import it.unipv.ingsfw.playfieldtech.exceptions.RisorsaNonTrovataException;
import it.unipv.ingsfw.playfieldtech.factory.UtenteFactory;
import it.unipv.ingsfw.playfieldtech.model.Cliente;
import it.unipv.ingsfw.playfieldtech.model.Squadra;
import it.unipv.ingsfw.playfieldtech.model.Utente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/*
* DAO specializzato per la gestione delle squadre. Si occupa di accedere al database per recuperare le squadre,
* i loro membri e per salvare nuove squadre.
* Include anche metodi per aggiungere e rimuovere membri da una squadra, e per eliminare una squadra. 
* Questo DAO è utilizzato principalmente dal SquadraController per gestire tutte le operazioni legate alle squadre,
*/
public class SquadraDAO {
    private Connection connection;
    private UtenteFactory utenteFactory;

    public SquadraDAO() {
        this.connection = DatabaseManager.getInstance().getConnection();
        this.utenteFactory = new UtenteFactory();
    }

    public Squadra getSquadraById(int idSquadra) throws RisorsaNonTrovataException, DataAccessException {
        String query = "SELECT * FROM Squadre WHERE id_squadra = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, idSquadra);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Squadra(
                        rs.getInt("id_squadra"),
                        rs.getString("nome_squadra"),
                        null, // Il capitano viene caricato solo se serve
                        rs.getInt("fk_capitano")
                    );
                } else {
                    throw new RisorsaNonTrovataException("Squadra non trovata con ID: " + idSquadra);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Errore recupero squadra per ID.", e);
        }
    }
 
    public List<Squadra> getSquadreByUtente(int idUtente) throws DataAccessException {
        List<Squadra> squadre = new ArrayList<>();
        String query = "SELECT s.id_squadra, s.nome_squadra, s.fk_capitano " +
                       "FROM Squadre s " +
                       "JOIN Squadre_Utenti su ON s.id_squadra = su.fk_squadra " +
                       "WHERE su.fk_utente = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, idUtente);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Squadra s = new Squadra(
                        rs.getInt("id_squadra"),
                        rs.getString("nome_squadra"),
                        null, 
                        rs.getInt("fk_capitano")
                    );
                    squadre.add(s);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Errore recupero squadre utente.", e);
        }
        return squadre;
    }

    public List<Cliente> getMembriBySquadra(int idSquadra) throws DataAccessException {
        List<Cliente> membri = new ArrayList<>();
        String query = "SELECT u.* " +
                       "FROM Utenti u " +
                       "JOIN Squadre_Utenti su ON u.id_utente = su.fk_utente " +
                       "WHERE su.fk_squadra = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, idSquadra);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Utente u = utenteFactory.creaUtente(
                        rs.getString("ruolo"),
                        rs.getInt("id_utente"),
                        rs.getString("username"),
                        "", 
                        rs.getString("nome"),
                        rs.getString("cognome")
                    );
                    //controllo in piu per possibili modifiche
                    if (u instanceof Cliente) {
                        membri.add((Cliente) u);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Errore recupero membri squadra.", e);
        }
        return membri;
    }

    public List<Squadra> getTutteLeSquadre() throws DataAccessException {
        List<Squadra> squadre = new ArrayList<>();
        String query = "SELECT * FROM Squadre ORDER BY nome_squadra";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Squadra s = new Squadra(
                    rs.getInt("id_squadra"),
                    rs.getString("nome_squadra"),
                    null,
                    rs.getInt("fk_capitano")
                );
                squadre.add(s);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Errore recupero tutte le squadre.", e);
        }
        return squadre;
    }

    public void salvaSquadra(Squadra squadra) throws DatiConflittoException, DataAccessException {
        String query = "INSERT INTO Squadre (nome_squadra, fk_capitano) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, squadra.getNomeSquadra());
            stmt.setInt(2, squadra.getIdCapitano());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    squadra.setIdSquadra(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { 
                throw new DatiConflittoException("Esiste già una squadra con il nome '" + squadra.getNomeSquadra() + "'");
            }
            throw new DataAccessException("Errore salvataggio squadra.", e);
        }
    }

    public void aggiungiMembro(int idSquadra, int idUtente) throws DataAccessException {
        String query = "INSERT INTO Squadre_Utenti (fk_squadra, fk_utente) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, idSquadra);
            stmt.setInt(2, idUtente);
            stmt.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) return; 
            throw new DataAccessException("Errore aggiunta membro.", e);
        }
    }

    public void rimuoviMembro(int idSquadra, int idUtente) throws DataAccessException {
        String query = "DELETE FROM Squadre_Utenti WHERE fk_squadra = ? AND fk_utente = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, idSquadra);
            stmt.setInt(2, idUtente);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Errore rimozione membro.", e);
        }
    }

    public void eliminaSquadra(int idSquadra) throws DataAccessException {
        String query = "DELETE FROM Squadre WHERE id_squadra = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, idSquadra);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Errore eliminazione squadra.", e);
        }
    }

    // In SquadraDAO.java

/**
 * Verifica se un utente appartiene a una determinata squadra.
 */
    public boolean isUtenteMembro(int idSquadra, int idUtente) throws DataAccessException {
        String query = "SELECT COUNT(*) FROM Squadre_Utenti WHERE fk_squadra = ? AND fk_utente = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, idSquadra);
            stmt.setInt(2, idUtente);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Errore verifica membership squadra", e);
        }
        return false;
    }
}