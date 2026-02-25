package it.unipv.ingsfw.playfieldtech.dao;

import it.unipv.ingsfw.playfieldtech.db.DatabaseManager;
import it.unipv.ingsfw.playfieldtech.exceptions.DataAccessException;
import it.unipv.ingsfw.playfieldtech.model.FasciaOraria;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

/*
* DAO specializzato per la gestione delle fasce orarie. Si occupa di accedere al database per recuperare le fasce orarie 
* configurate, che sono necessarie per la prenotazione degli impianti e per la visualizzazione del calendario.
*/
public class FasciaOrariaDAO {
    private Connection connection;

    public FasciaOrariaDAO() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    public List<FasciaOraria> getTutteFasceOrarie() throws DataAccessException {
        List<FasciaOraria> fasce = new ArrayList<>();
        String query = "SELECT * FROM FasceOrarie " +
                       "ORDER BY FIELD(giorno_settimana, 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'), ora_inizio";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                fasce.add(new FasciaOraria(
                    rs.getInt("id_fascia"),
                    DayOfWeek.valueOf(rs.getString("giorno_settimana").toUpperCase()),
                    rs.getTime("ora_inizio").toLocalTime(),
                    rs.getTime("ora_fine").toLocalTime(),
                    rs.getDouble("prezzo_base")
                ));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Errore durante il recupero delle fasce orarie.", e);
        }
        return fasce;
    }
}