package it.unipv.ingsfw.playfieldtech.dao;

import it.unipv.ingsfw.playfieldtech.db.DatabaseManager;
import it.unipv.ingsfw.playfieldtech.exceptions.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
* DAO specializzato per la gestione della tabella di associazione tra impianti e fasce orarie. Si occupa di 
* accedere al database per recuperare la mappa di disponibilità degli impianti in base alle fasce orarie
*/
public class ImpiantiFasceDAO {
    private Connection connection;

    public ImpiantiFasceDAO() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    public Map<Integer, List<Integer>> getMappaDisponibilita() throws DataAccessException {
        Map<Integer, List<Integer>> mappa = new HashMap<>();
        String query = "SELECT fk_campo, fk_fascia FROM Impianti_Fasce";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int idCampo = rs.getInt("fk_campo");
                int idFascia = rs.getInt("fk_fascia");
                // Costruiamo la mappa che associa a ogni campo la lista delle fasce orarie disponibili
                List<Integer> fasce = mappa.get(idCampo);
                if (fasce == null) { // Se non esiste ancora una lista per questo campo, la creiamo
                    fasce = new ArrayList<>(); 
                    mappa.put(idCampo, fasce);
                }
                fasce.add(idFascia); // Aggiungiamo la fascia alla lista delle fasce disponibili per questo campo
            }
        } catch (SQLException e) {
            throw new DataAccessException("Errore durante il recupero della mappa di disponibilità.", e);
        }
        return mappa;
    }
}