package it.unipv.ingsfw.playfieldtech.dao;

import it.unipv.ingsfw.playfieldtech.db.DatabaseManager;
import it.unipv.ingsfw.playfieldtech.exceptions.DataAccessException;
import it.unipv.ingsfw.playfieldtech.factory.UtenteFactory;
import it.unipv.ingsfw.playfieldtech.model.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/*
* DAO specializzato per la gestione delle prenotazioni. Si occupa di accedere al database per salvare nuove prenotazioni, 
* recuperare le prenotazioni per data (per il calendario) e recuperare lo storico delle prenotazioni di un utente.
* Include anche metodi specifici per la gestione dei tornei, come salvare una prenotazione di torneo, eliminare le 
* prenotazioni associate a una squadra (quando una squadra viene eliminata) e liberare i campi associati a un torneo quando 
* questo viene cancellato.
* Questo DAO è utilizzato principalmente dal PrenotazioneFacade per gestire tutte le operazioni legate alle prenotazioni, 
* mantenendo così una separazione chiara tra la logica di business (gestita dalle Facade) e la logica di accesso ai dati (gestita dai DAO).
*/
public class PrenotazioneDAO {
    private Connection connection;

    public PrenotazioneDAO() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    public void salvaPrenotazione(Prenotazione prenotazione) throws DataAccessException {
        String queryPrenotazione = "INSERT INTO Prenotazioni (data_prenotazione, nome_squadra_a, nome_squadra_b, pagamento_confermato, fk_cliente, fk_campo, fk_fascia) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String queryGiocatori = "INSERT INTO Prenotazioni_Giocatori (fk_prenotazione, fk_giocatore) VALUES (?, ?)";
        
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement stmt = connection.prepareStatement(queryPrenotazione, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setDate(1, Date.valueOf(prenotazione.getData()));
                stmt.setString(2, prenotazione.getNomeSquadraA());
                stmt.setString(3, prenotazione.getNomeSquadraB());
                stmt.setBoolean(4, prenotazione.isPagamentoConfermato());
                stmt.setInt(5, prenotazione.getCreatore().getIdUtente());
                stmt.setInt(6, prenotazione.getImpianto().getIdCampo());
                stmt.setInt(7, prenotazione.getFasciaOraria().getIdFascia());
                stmt.executeUpdate();
                // Recuperiamo l'ID generato per la prenotazione appena inserita
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        // Impostiamo l'ID della prenotazione nell'oggetto, così è disponibile per i passaggi successivi (es. inserimento giocatori)
                        prenotazione.setIdPrenotazione(rs.getInt(1));
                    }
                }
            }

            // Salviamo i giocatori associati (fondamentale per i tornei)
            // Controllo di sicurezza per evitare errori se la lista è null o vuota
            if (prenotazione.getGiocatori() != null && !prenotazione.getGiocatori().isEmpty()) { 
                try (PreparedStatement stmtGiocatori = connection.prepareStatement(queryGiocatori)) {
                    for (Cliente c : prenotazione.getGiocatori()) {
                        stmtGiocatori.setInt(1, prenotazione.getIdPrenotazione());
                        stmtGiocatori.setInt(2, c.getIdUtente());
                        stmtGiocatori.addBatch(); 
                    }
                    stmtGiocatori.executeBatch();
                }
            }
            connection.commit();
        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            throw new DataAccessException("Errore salvataggio prenotazione.", e);
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
    
    /**
     * Recupera le prenotazioni per data (Usato dal calendario)
     */
    public List<Prenotazione> getPrenotazioniByDate(LocalDate date) throws DataAccessException {
        List<Prenotazione> list = new ArrayList<>();
        String query = "SELECT * FROM Prenotazioni WHERE data_prenotazione = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDate(1, Date.valueOf(date));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Prenotazione p = new Prenotazione(0, date, null, null, null);
                    p.setIdPrenotazione(rs.getInt("id_prenotazione"));
                    // Caricamento pigro per performance, carichiamo solo gli IDFK
                    p.setFkCampo(rs.getInt("fk_campo"));
                    p.setFkFascia(rs.getInt("fk_fascia"));
                    list.add(p);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Errore recupero prenotazioni per data.", e);
        }
        return list;
    }

    /**
     * Recupera lo storico. MODIFICATO per includere le partite in cui l'utente è GIOCATORE.
     */
    public List<Prenotazione> getTutteLePrenotazioniPerUtente(int idUtente) throws DataAccessException {
        List<Prenotazione> tutteLePrenotazioni = new ArrayList<>();
        
        // JOIN con Prenotazioni_Giocatori e filtro OR
        String query = "SELECT DISTINCT p.*, u.id_utente, u.username AS creatore_username, u.ruolo AS creatore_ruolo, " +
                       "i.nome AS nome_impianto, f.ora_inizio, f.ora_fine " +
                       "FROM Prenotazioni p " +
                       "JOIN Utenti u ON p.fk_cliente = u.id_utente " +
                       "JOIN ImpiantiSportivi i ON p.fk_campo = i.id_campo " +
                       "JOIN FasceOrarie f ON p.fk_fascia = f.id_fascia " +
                       "LEFT JOIN Prenotazioni_Giocatori pg ON p.id_prenotazione = pg.fk_prenotazione " +
                       "WHERE p.fk_cliente = ? OR pg.fk_giocatore = ? " +
                       "ORDER BY p.data_prenotazione DESC, f.ora_inizio ASC";

        UtenteFactory factory = new UtenteFactory();
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, idUtente);
            stmt.setInt(2, idUtente);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Utente creatore = factory.creaUtente(rs.getString("creatore_ruolo"), rs.getInt("id_utente"), rs.getString("creatore_username"), "", "", "");
                    ImpiantoSportivo impianto = new ImpiantoSportivo(0, rs.getString("nome_impianto"), "");
                    FasciaOraria fascia = new FasciaOraria(0, null, rs.getTime("ora_inizio").toLocalTime(), rs.getTime("ora_fine").toLocalTime(), 0);
                    
                    Prenotazione p = new Prenotazione(rs.getInt("id_prenotazione"), rs.getDate("data_prenotazione").toLocalDate(), creatore, impianto, fascia);
                    p.setNomeSquadraA(rs.getString("nome_squadra_a"));
                    p.setNomeSquadraB(rs.getString("nome_squadra_b"));
                    tutteLePrenotazioni.add(p);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Errore recupero storico prenotazioni.", e);
        }
        return tutteLePrenotazioni;
    }

    // Metodo extra per l'admin
    public List<Prenotazione> getTutteLePrenotazioni() throws DataAccessException {
        List<Prenotazione> list = new ArrayList<>();
        String query = "SELECT p.*, u.username, i.nome AS nome_impianto, f.ora_inizio, f.ora_fine " +
                       "FROM Prenotazioni p " +
                       "JOIN Utenti u ON p.fk_cliente = u.id_utente " +
                       "JOIN ImpiantiSportivi i ON p.fk_campo = i.id_campo " +
                       "JOIN FasceOrarie f ON p.fk_fascia = f.id_fascia";
        // Implementazione semplificata per brevità, simile a sopra ma senza WHERE
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while(rs.next()) {
                // Costruzione oggetto...
                 ImpiantoSportivo impianto = new ImpiantoSportivo(0, rs.getString("nome_impianto"), "");
                 FasciaOraria fascia = new FasciaOraria(0, null, rs.getTime("ora_inizio").toLocalTime(), rs.getTime("ora_fine").toLocalTime(), 0);
                 Prenotazione p = new Prenotazione(rs.getInt("id_prenotazione"), rs.getDate("data_prenotazione").toLocalDate(), null, impianto, fascia);
                 p.setNomeSquadraA(rs.getString("nome_squadra_a"));
                 p.setNomeSquadraB(rs.getString("nome_squadra_b"));
                 list.add(p);
            }
        } catch (SQLException e) { throw new DataAccessException("Errore", e); }
        return list;
    }
    
    public int salvaPrenotazioneTorneo(Prenotazione p) throws DataAccessException {
        salvaPrenotazione(p); // Riutilizza la logica completa
        return p.getIdPrenotazione();
    }

    /**
     * Elimina tutte le prenotazioni in cui è coinvolta una specifica squadra (come casa o ospite).
     */
    public void eliminaPrenotazioniPerSquadra(String nomeSquadra) throws DataAccessException {
        String query = "DELETE FROM Prenotazioni WHERE nome_squadra_a = ? OR nome_squadra_b = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, nomeSquadra);
            stmt.setString(2, nomeSquadra);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Errore durante l'eliminazione delle prenotazioni della squadra: " + nomeSquadra, e);
        }
    }

    public boolean isUtenteOccupato(int idUtente, LocalDate data, int idFascia) throws DataAccessException {
        String query = "SELECT COUNT(*) FROM Prenotazioni p " +
                    "JOIN Prenotazioni_Giocatori pg ON p.id_prenotazione = pg.fk_prenotazione " +
                    "WHERE pg.fk_giocatore = ? " +
                    "AND p.data_prenotazione = ? " +
                    "AND p.fk_fascia = ?";
                    
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, idUtente);
            stmt.setDate(2, Date.valueOf(data));
            stmt.setInt(3, idFascia);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0; // Se > 0, l'utente è occupato
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Errore controllo disponibilità giocatore", e);
        }
        return false;
    }

    
    /*Riassunto della logica correttiva
    Con questo codice, il flusso quando clicchi "Elimina Torneo" sarà:
    1)Il programma si segna i numeri delle prenotazioni da cancellare (es. Prenotazione #10 e #11).
    2)Va nelle Partite e cancella il riferimento (La Semifinale A non punta più alla Prenotazione #10).
    3)Va nei Giocatori e cancella chi partecipava alla Prenotazione #10.
    4)Cancella la Prenotazione #10 (Il campo torna verde).
    5)(Successivamente nel TorneoDAO) Cancella le Partite e il Torneo.*/
    public void eliminaPrenotazioniDiTorneo(int idTorneo) throws DataAccessException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // 1. RECUPERIAMO GLI ID DELLE PRENOTAZIONI DA CANCELLARE
            // Dobbiamo salvarli prima di sganciare le partite, altrimenti li perdiamo.
            List<Integer> idsPrenotazioni = new ArrayList<>();
            String querySelect = "SELECT fk_prenotazione FROM Partite WHERE fk_torneo = ? AND fk_prenotazione IS NOT NULL";
            
            stmt = connection.prepareStatement(querySelect);
            stmt.setInt(1, idTorneo);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                idsPrenotazioni.add(rs.getInt("fk_prenotazione"));
            }
            rs.close();
            stmt.close();

            // Se non ci sono prenotazioni, usciamo subito
            if (idsPrenotazioni.isEmpty()) {
                return;
            }

            // Costruiamo una stringa per la clausola IN (...) es: "1, 2, 5"
            // Questo serve per cancellare in blocco
            StringBuilder idListBuilder = new StringBuilder();
            for (int i = 0; i < idsPrenotazioni.size(); i++) {
                idListBuilder.append(idsPrenotazioni.get(i));
                if (i < idsPrenotazioni.size() - 1) {
                    idListBuilder.append(",");
                }
            }
            String idList = idListBuilder.toString();

            // 2. SGANCIAMO LE PARTITE (UPDATE a NULL)
            // Diciamo alle partite: "Non avete più una prenotazione associata"
            // Questo evita l'errore di Foreign Key quando cancelleremo la prenotazione
            String queryUnlink = "UPDATE Partite SET fk_prenotazione = NULL WHERE fk_torneo = ?";
            stmt = connection.prepareStatement(queryUnlink);
            stmt.setInt(1, idTorneo);
            stmt.executeUpdate();
            stmt.close();

            // 3. CANCELLIAMO I GIOCATORI ASSOCIATI (Tabella Prenotazioni_Giocatori)
            // Se non cancelliamo questi figli, non possiamo cancellare il padre (Prenotazione)
            String queryDeletePlayers = "DELETE FROM Prenotazioni_Giocatori WHERE fk_prenotazione IN (" + idList + ")";
            stmt = connection.prepareStatement(queryDeletePlayers);
            stmt.executeUpdate();
            stmt.close();

            // 4. FINALMENTE CANCELLIAMO LE PRENOTAZIONI
            // Ora che nessuno le punta più, possiamo rimuoverle e liberare i campi
            String queryDeleteReservations = "DELETE FROM Prenotazioni WHERE id_prenotazione IN (" + idList + ")";
            stmt = connection.prepareStatement(queryDeleteReservations);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Errore durante la liberazione dei campi del torneo: " + e.getMessage(), e);
        } finally {
            // Chiusura risorse manuale per sicurezza in questo blocco complesso
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
        }
    }
}