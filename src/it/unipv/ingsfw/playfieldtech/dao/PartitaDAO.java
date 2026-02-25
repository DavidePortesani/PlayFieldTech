package it.unipv.ingsfw.playfieldtech.dao;

import it.unipv.ingsfw.playfieldtech.db.DatabaseManager;
import it.unipv.ingsfw.playfieldtech.exceptions.DataAccessException;
import it.unipv.ingsfw.playfieldtech.exceptions.RisorsaNonTrovataException;
import it.unipv.ingsfw.playfieldtech.factory.DaoFactory;
import it.unipv.ingsfw.playfieldtech.model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PartitaDAO {
    private Connection connection;
    private SquadraDAO squadraDAO; 
    
    public PartitaDAO() {
        this.connection = DatabaseManager.getInstance().getConnection();
        this.squadraDAO = DaoFactory.getInstance().getSquadraDAO();
    }

    /*
    * Crea una partita vuota nel database, associata a un torneo e a un turno specifico.
    */
    public void creaPartitaVuota(Partita partita) throws DataAccessException {
        String query = "INSERT INTO Partite (fk_torneo, turno) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, partita.getIdTorneo());
            stmt.setInt(2, partita.getTurno());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) partita.setIdPartita(rs.getInt(1)); // columnIndex 1 corrisponde all'ID generato automaticamente
            }
        } catch (SQLException e) {
            throw new DataAccessException("Errore creazione partita vuota.", e);
        }
    }
    // collega la partita appena creata al suo successivo (es. semifinale -> finale)
    public void collegaPartitaSuccessiva(int idPartitaCorrente, int idPartitaSuccessiva, String slot) throws DataAccessException {
        String query = "UPDATE Partite SET fk_partita_successiva = ?, slot_vincitore_successivo = ? WHERE id_partita = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, idPartitaSuccessiva); // La partita del turno corrente viene collegata alla partita del turno successivo
            stmt.setString(2, slot); // "CASA" o "OSPITE"
            stmt.setInt(3, idPartitaCorrente); // La partita del turno corrente viene aggiornata per sapere dove mandare il vincitore
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Errore collegamento partita successiva.", e);
        }
    }
    /*
    * Aggiorna le squadre di una partita (usato per assegnare le squadre alle partite del primo turno e per avanzare i vincitori)
    */
    public void aggiornaSquadre(int idPartita, int idCasa, int idOspite) throws DataAccessException {
        String query = "UPDATE Partite SET fk_squadra_casa = ?, fk_squadra_ospite = ? WHERE id_partita = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, idCasa);
            stmt.setInt(2, idOspite);
            stmt.setInt(3, idPartita);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Errore aggiornamento squadre partita.", e);
        }
    }
    
    // Aggiorna la prenotazione associata a una partita (usato per assegnare un campo e un orario alla partita)
    public void aggiornaConPrenotazione(int idPartita, int idPrenotazione) throws DataAccessException {
        String query = "UPDATE Partite SET fk_prenotazione = ? WHERE id_partita = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, idPrenotazione);
            stmt.setInt(2, idPartita);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Errore aggiornamento prenotazione partita.", e);
        }
    }
    
    public List<Partita> getPartiteByTorneo(int idTorneo) throws DataAccessException {
        List<Partita> partite = new ArrayList<>();
        // Query complessa per recuperare i nomi senza dover fare N query separate
        String query = "SELECT p.*, " +
                       "sc.id_squadra AS id_casa, sc.nome_squadra AS nome_casa, sc.fk_capitano AS cap_casa, " +
                       "so.id_squadra AS id_ospite, so.nome_squadra AS nome_ospite, so.fk_capitano AS cap_ospite, " +
                       "pr.id_prenotazione, pr.data_prenotazione, i.nome AS nome_impianto, f.ora_inizio, f.ora_fine " +
                       "FROM Partite p " +
                       "LEFT JOIN Squadre sc ON p.fk_squadra_casa = sc.id_squadra " +
                       "LEFT JOIN Squadre so ON p.fk_squadra_ospite = so.id_squadra " +
                       "LEFT JOIN Prenotazioni pr ON p.fk_prenotazione = pr.id_prenotazione " +
                       "LEFT JOIN ImpiantiSportivi i ON pr.fk_campo = i.id_campo " +
                       "LEFT JOIN FasceOrarie f ON pr.fk_fascia = f.id_fascia " +
                       "WHERE p.fk_torneo = ? ORDER BY p.turno DESC, pr.data_prenotazione, f.ora_inizio";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, idTorneo);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Squadra casa = null;
                    if (rs.getObject("id_casa") != null) {
                        casa = new Squadra(rs.getInt("id_casa"), rs.getString("nome_casa"), null, rs.getInt("cap_casa"));
                    }
                    Squadra ospite = null;
                    if (rs.getObject("id_ospite") != null) {
                        ospite = new Squadra(rs.getInt("id_ospite"), rs.getString("nome_ospite"), null, rs.getInt("cap_ospite"));
                    }
                    Prenotazione prenotazione = null;
                    if (rs.getObject("id_prenotazione") != null) {
                        ImpiantoSportivo impianto = new ImpiantoSportivo(0, rs.getString("nome_impianto"), "");
                        FasciaOraria fascia = new FasciaOraria(0, null, rs.getTime("ora_inizio").toLocalTime(), rs.getTime("ora_fine").toLocalTime(), 0);
                        prenotazione = new Prenotazione(rs.getInt("id_prenotazione"), rs.getDate("data_prenotazione").toLocalDate(), null, impianto, fascia);
                    }
                    Partita partita = new Partita(idTorneo, rs.getInt("turno"));
                    partita.setIdPartita(rs.getInt("id_partita"));
                    partita.setSquadraCasa(casa);
                    partita.setSquadraOspite(ospite);
                    partita.setPrenotazione(prenotazione);
                    partita.setRisultatoCasa((Integer) rs.getObject("risultato_casa"));
                    partita.setRisultatoOspite((Integer) rs.getObject("risultato_ospite"));
                    partite.add(partita);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Errore recupero partite del torneo.", e);
        }
        return partite;
    }

    public void aggiornaRisultato(int idPartita, int golCasa, int golOspite) throws DataAccessException {
        String query = "UPDATE Partite SET risultato_casa = ?, risultato_ospite = ? WHERE id_partita = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, golCasa);
            stmt.setInt(2, golOspite);
            
            stmt.setInt(3, idPartita);

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Errore aggiornamento risultato partita.", e);
        }
    }

    public Partita getPartitaById(int idPartita) throws RisorsaNonTrovataException, DataAccessException {
        String query = "SELECT p.*, next_p.id_partita AS next_id, " +
                       "pr.id_prenotazione AS pr_id, pr.data_prenotazione, " +
                       "i.nome AS nome_impianto, f.ora_inizio, f.ora_fine " +
                       "FROM Partite p " +
                       "LEFT JOIN Partite next_p ON p.fk_partita_successiva = next_p.id_partita " +
                       "LEFT JOIN Prenotazioni pr ON p.fk_prenotazione = pr.id_prenotazione " +
                       "LEFT JOIN ImpiantiSportivi i ON pr.fk_campo = i.id_campo " +
                       "LEFT JOIN FasceOrarie f ON pr.fk_fascia = f.id_fascia " +
                       "WHERE p.id_partita = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, idPartita);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Partita p = new Partita(rs.getInt("fk_torneo"), rs.getInt("turno"));
                    p.setIdPartita(rs.getInt("id_partita"));
                    
                    if(rs.getObject("fk_squadra_casa") != null)
                        p.setSquadraCasa(squadraDAO.getSquadraById(rs.getInt("fk_squadra_casa")));
                    
                    if(rs.getObject("fk_squadra_ospite") != null)
                        p.setSquadraOspite(squadraDAO.getSquadraById(rs.getInt("fk_squadra_ospite")));
                    
                    p.setRisultatoCasa((Integer) rs.getObject("risultato_casa"));
                    p.setRisultatoOspite((Integer) rs.getObject("risultato_ospite"));
                    p.setSlotVincitoreSuccessivo(rs.getString("slot_vincitore_successivo"));
                    
                    if (rs.getObject("pr_id") != null) {
                        ImpiantoSportivo impianto = new ImpiantoSportivo(0, rs.getString("nome_impianto"), "");
                        FasciaOraria fascia = new FasciaOraria(0, null, rs.getTime("ora_inizio").toLocalTime(), rs.getTime("ora_fine").toLocalTime(), 0);
                        Prenotazione prenotazione = new Prenotazione(rs.getInt("pr_id"), rs.getDate("data_prenotazione").toLocalDate(), null, impianto, fascia);
                        p.setPrenotazione(prenotazione);
                    }
                    if (rs.getObject("next_id") != null) {
                        Partita next = new Partita(rs.getInt("fk_torneo"), 0); 
                        next.setIdPartita(rs.getInt("next_id"));
                        p.setPartitaSuccessiva(next);
                    }
                    return p;
                } else {
                    throw new RisorsaNonTrovataException("Partita non trovata con ID: " + idPartita);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Errore recupero partita by ID.", e);
        }
    }

    public void avanzaVincitore(int idSquadraVincitrice, int idPartitaSuccessiva, String slot) throws DataAccessException {
        // slot indica se la squadra vincitrice va assegnata come "CASA" o "OSPITE" nella partita successiva
        String colonna = slot.equals("CASA") ? "fk_squadra_casa" : "fk_squadra_ospite"; 
        String query = "UPDATE Partite SET " + colonna + " = ? WHERE id_partita = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, idSquadraVincitrice);
            stmt.setInt(2, idPartitaSuccessiva);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Errore avanzamento vincitore.", e);
        }
    }
}