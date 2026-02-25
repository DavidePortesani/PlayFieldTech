package it.unipv.ingsfw.playfieldtech.dao;

import it.unipv.ingsfw.playfieldtech.db.DatabaseManager;
import it.unipv.ingsfw.playfieldtech.exceptions.DataAccessException;
import it.unipv.ingsfw.playfieldtech.model.FasciaOraria;
import it.unipv.ingsfw.playfieldtech.model.ImpiantoSportivo;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/*
* DAO specializzato per la gestione degli impianti sportivi. Si occupa di accedere al database per recuperare 
* la lista degli impianti sportivi configurati, e per salvare nuovi impianti sportivi insieme alle fasce orarie in cui sono disponibili. 
* Questo DAO è utilizzato principalmente dal controller dell'Admin per gestire gli impianti sportivi,
*/
public class ImpiantoSportivoDAO {
    private Connection connection;

    public ImpiantoSportivoDAO() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    public List<ImpiantoSportivo> getTuttiImpianti() throws DataAccessException {
        List<ImpiantoSportivo> impianti = new ArrayList<>();
        String query = "SELECT * FROM ImpiantiSportivi ORDER BY nome";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                // Estrazione dei dati grezzi dalla riga corrente del database
                int idCampoCorrente = rs.getInt("id_campo");
                String nomeImpiantoCorrente = rs.getString("nome");
                String tipoSportCorrente = rs.getString("tipo_sport");
                
                // Creazione dell'oggetto Java utilizzando i dati estratti
                ImpiantoSportivo nuovoImpianto = new ImpiantoSportivo(idCampoCorrente, nomeImpiantoCorrente, tipoSportCorrente);
                
                // Aggiunta dell'oggetto "assemblato" alla lista da restituire
                impianti.add(nuovoImpianto);            
            }
        } catch (SQLException e) {
            throw new DataAccessException("Errore durante il recupero degli impianti.", e);
        }
        return impianti;
    }

    public void salvaImpiantoConFasce(ImpiantoSportivo impianto, List<FasciaOraria> fasceSelezionate) throws DataAccessException {
        String queryImpianto = "INSERT INTO ImpiantiSportivi (nome, tipo_sport) VALUES (?, ?)";
        String queryLink = "INSERT INTO Impianti_Fasce (fk_campo, fk_fascia) VALUES (?, ?)";
        try {
            connection.setAutoCommit(false);
            int idNuovoCampo;
            try (PreparedStatement stmtImpianto = connection.prepareStatement(queryImpianto, Statement.RETURN_GENERATED_KEYS)) {
                stmtImpianto.setString(1, impianto.getNome());
                stmtImpianto.setString(2, impianto.getTipoSport());
                stmtImpianto.executeUpdate();
                try (ResultSet generatedKeys = stmtImpianto.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        idNuovoCampo = generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("Creazione impianto fallita, nessun ID ottenuto.");
                    }
                }
            }
            try (PreparedStatement stmtLink = connection.prepareStatement(queryLink)) {
                for (FasciaOraria fascia : fasceSelezionate) {
                    stmtLink.setInt(1, idNuovoCampo);
                    stmtLink.setInt(2, fascia.getIdFascia());
                    stmtLink.addBatch();
                }
                stmtLink.executeBatch();
            }
            connection.commit();
        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ex) { throw new DataAccessException("Errore durante il rollback.", ex); }
            throw new DataAccessException("Errore durante la transazione di salvataggio impianto.", e);
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException e) { throw new DataAccessException("Errore nel ripristino auto-commit.", e); }
        }
    }
}