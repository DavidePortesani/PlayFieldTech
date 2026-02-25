package it.unipv.ingsfw.playfieldtech.dao;

import it.unipv.ingsfw.playfieldtech.db.DatabaseManager;
import it.unipv.ingsfw.playfieldtech.exceptions.DataAccessException;
import it.unipv.ingsfw.playfieldtech.exceptions.RisorsaNonTrovataException;
import it.unipv.ingsfw.playfieldtech.model.Squadra;
import it.unipv.ingsfw.playfieldtech.model.Torneo;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/*
* DAO specializzato per la gestione dei tornei. Si occupa di accedere al database per creare nuovi tornei,
* recuperare la lista dei tornei, aggiornare lo stato di un torneo e eliminare un torneo.
* Include anche un metodo per recuperare le squadre partecipanti a un torneo specifico.
* Questo DAO è utilizzato principalmente dal TorneoController per gestire tutte le operazioni legate ai tornei, 
* come la creazione, la visualizzazione, l'aggiornamento e l'eliminazione dei tornei.
*/
public class TorneoDAO {
    private Connection connection;

    public TorneoDAO() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    public void creaTorneo(Torneo torneo, List<Squadra> squadre, int idAdmin) throws DataAccessException {
        String queryTorneo = "INSERT INTO Tornei (nome, data_inizio, stato, fk_admin) VALUES (?, ?, 'BOZZA', ?)";
        String querySquadre = "INSERT INTO Tornei_Squadre (fk_torneo, fk_squadra) VALUES (?, ?)";
        try {
            // Iniziamo una transazione per assicurarci che tutte le operazioni vengano eseguite correttamente
            // disattivandolo permette di far aspettare tutte le operazioni prima di confermare con commit() essendo che 
            // stiamo lavorando su più tabelle (Tornei e Tornei_Squadre)
            connection.setAutoCommit(false);           
            int idNuovoTorneo;
            //con RETURN_GENERATED_KEYS possiamo recuperare l'ID del torneo appena creato
            try (PreparedStatement stmt = connection.prepareStatement(queryTorneo, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, torneo.getNome());
                stmt.setDate(2, Date.valueOf(torneo.getDataInizio()));
                stmt.setInt(3, idAdmin); // parameterIndex 3 corrisponde al terzo punto interrogativo nella query (fk_admin)
                stmt.executeUpdate(); // Esegue l'inserimento del torneo e genera una nuova riga nel database
                try (ResultSet rs = stmt.getGeneratedKeys()) { // Recupera l'ID generato automaticamente per il nuovo torneo
                    if (rs.next()) { // Se è stato generato un ID, lo recuperiamo e lo impostiamo nell'oggetto torneo
                        idNuovoTorneo = rs.getInt(1);
                        // Imposta l'ID del torneo appena creato nell'oggetto Torneo per utilizzarlo nel metodo
                        //generaCalendario() per sapere di quale torneo stiamo generando il calendario
                        torneo.setIdTorneo(idNuovoTorneo); 
                    } else throw new SQLException("Creazione torneo fallita.");
                }
            }
            try (PreparedStatement stmtSquadre = connection.prepareStatement(querySquadre)) {
                for (Squadra s : squadre) {
                    stmtSquadre.setInt(1, idNuovoTorneo);
                    stmtSquadre.setInt(2, s.getIdSquadra());
                    stmtSquadre.addBatch();
                }
                // executeBatch() esegue tutte le operazioni di inserimento in una volta sola, 
                // migliorando le prestazioni rispetto a eseguire ogni inserimento singolarmente 
                // essendoci un ciclo for che itera sulle squadre da associare al torneo
                stmtSquadre.executeBatch();
            }
            // Se tutto è andato bene, confermiamo la transazione con commit(), rendendo permanenti tutte le modifiche al database
            connection.commit(); 
        } catch (SQLException e) {
            // Se qualcosa va storto, annulliamo tutto per evitare dati incoerenti nel database (es. torneo creato ma senza squadre associate)
            //rollback() cancella tutte le operazioni fatte dopo l'ultimo commit()
            try { connection.rollback(); } catch (SQLException ex) { throw new DataAccessException("Errore durante il rollback.", ex); }
            throw new DataAccessException("Errore durante la transazione di creazione torneo.", e);
        } finally {// Ripristiniamo l'auto-commit al suo stato originale per evitare effetti collaterali su altre operazioni future. Lo fa sempre, sia in caso di successo che di errore
            try { connection.setAutoCommit(true); } catch (SQLException e) { throw new DataAccessException("Errore nel ripristino auto-commit.", e); }
        }
    }
    
    public List<Torneo> getTuttiITornei() throws DataAccessException {
        List<Torneo> tornei = new ArrayList<>();
        String query = "SELECT * FROM Tornei ORDER BY data_inizio DESC";
        try (PreparedStatement stmt = connection.prepareStatement(query); 
             ResultSet rs = stmt.executeQuery()) {
            // Per ogni torneo recuperato, creiamo un oggetto Torneo e lo aggiungiamo alla lista
            while (rs.next()) {
                Torneo t = new Torneo(
                    rs.getInt("id_torneo"),
                    rs.getString("nome"),
                    rs.getDate("data_inizio").toLocalDate() // Convertiamo da java.sql.Date a java.time.LocalDate 
                );
                t.setStato(rs.getString("stato")); // Impostiamo lo stato del torneo (Bozza, Attivo, Concluso)
                tornei.add(t);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Errore nel recupero di tutti i tornei.", e);
        }
        return tornei;
    }
    /*
    * Recupera la lista delle squadre partecipanti a un torneo specifico, dato il suo ID.
    */
    public List<Squadra> getSquadreByTorneo(int idTorneo) throws DataAccessException {
        List<Squadra> squadre = new ArrayList<>();
        String query = "SELECT s.* FROM Squadre s " +
                       "JOIN Tornei_Squadre ts ON s.id_squadra = ts.fk_squadra " +
                       "WHERE ts.fk_torneo = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) { 
            stmt.setInt(1, idTorneo);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    squadre.add(new Squadra(
                        rs.getInt("id_squadra"),
                        rs.getString("nome_squadra"),
                        null,
                        rs.getInt("fk_capitano")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Errore nel recupero squadre del torneo.", e);
        }
        return squadre;
    }
    
    public void aggiornaStato(int idTorneo, String stato) throws DataAccessException {
        String query = "UPDATE Tornei SET stato = ? WHERE id_torneo = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, stato);
            stmt.setInt(2, idTorneo);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Errore aggiornamento stato torneo.", e);
        }
    }

    public void aggiornaVincitore(int idTorneo, int idSquadraVincitrice) throws DataAccessException {
        String query = "UPDATE Tornei SET fk_vincitore = ? WHERE id_torneo = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, idSquadraVincitrice);
            stmt.setInt(2, idTorneo);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Errore aggiornamento vincitore torneo.", e);
        }
    }

    /**
     * Recupera la lista dei tornei a cui partecipa un determinato utente (tramite le sue squadre).
     */
    public List<Torneo> getTorneiPerUtente(int idUtente) throws DataAccessException {
        List<Torneo> tornei = new ArrayList<>();
        
        String query = "SELECT DISTINCT t.* " +
                       "FROM Tornei t " +
                       "JOIN Tornei_Squadre ts ON t.id_torneo = ts.fk_torneo " +
                       "JOIN Squadre s ON ts.fk_squadra = s.id_squadra " +
                       "JOIN Squadre_Utenti su ON s.id_squadra = su.fk_squadra " +
                       "WHERE su.fk_utente = ? AND t.stato = 'ATTIVO' OR t.stato = 'CONCLUSO' " + 
                       "ORDER BY t.data_inizio DESC";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, idUtente);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Torneo t = new Torneo(
                        rs.getInt("id_torneo"),
                        rs.getString("nome"),
                        rs.getDate("data_inizio").toLocalDate()
                    );
                    t.setStato(rs.getString("stato"));
                    tornei.add(t);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Errore recupero tornei utente.", e);
        }
        return tornei;
    }

    public void eliminaTorneo(int idTorneo) throws DataAccessException {
        String querySquadre = "DELETE FROM Tornei_Squadre WHERE fk_torneo = ?";
        String queryPartite = "DELETE FROM Partite WHERE fk_torneo = ?";
        String queryTorneo = "DELETE FROM Tornei WHERE id_torneo = ?";
        
        try {
            connection.setAutoCommit(false); // Iniziamo una transazione per sicurezza

            // Eliminiamo le associazioni delle squadre al torneo
            try (PreparedStatement stmt = connection.prepareStatement(querySquadre)) {
                stmt.setInt(1, idTorneo);
                stmt.executeUpdate();
            }

            // Eliminiamo le partite del torneo (Tabellone)
            // Nota: Le prenotazioni collegate le abbiamo già eliminate nel Manager prima di chiamare questo metodo
            try (PreparedStatement stmt = connection.prepareStatement(queryPartite)) {
                stmt.setInt(1, idTorneo);
                stmt.executeUpdate();
            }

            // Infine, eliminiamo il torneo stesso
            try (PreparedStatement stmt = connection.prepareStatement(queryTorneo)) {
                stmt.setInt(1, idTorneo);
                stmt.executeUpdate();
            }

            connection.commit(); // Confermiamo tutte le cancellazioni
            
        } catch (SQLException e) {
            try {
                connection.rollback(); // Se qualcosa va storto, annulliamo tutto
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            throw new DataAccessException("Errore durante l'eliminazione del torneo (Vincoli FK): " + e.getMessage(), e);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public Torneo getTorneoById(int idTorneo) throws RisorsaNonTrovataException, DataAccessException {
        String query = "SELECT * FROM Tornei WHERE id_torneo = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, idTorneo);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Torneo t = new Torneo(
                        rs.getInt("id_torneo"),
                        rs.getString("nome"),
                        rs.getDate("data_inizio").toLocalDate()
                    );
                    t.setStato(rs.getString("stato")); // Imposta lo stato (Bozza, Attivo, ecc.)
                    return t;
                } else {
                    throw new RisorsaNonTrovataException("Torneo non trovato con ID: " + idTorneo);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Errore recupero torneo per ID.", e);
        }
    }
}