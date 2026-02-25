package test.it.unipv.ingsfw.playfieldtech.conflict;

import it.unipv.ingsfw.playfieldtech.dao.PrenotazioneDAO;
import it.unipv.ingsfw.playfieldtech.exceptions.DataAccessException;
import it.unipv.ingsfw.playfieldtech.factory.DaoFactory;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;

class ConflictTest {

    @Test
    void testIsUtenteOccupato() {
        // Otteniamo il DAO dalla Factory
        PrenotazioneDAO dao = DaoFactory.getInstance().getPrenotazioneDAO();
        
        // Dati fittizi per il test (modifica questi valori con ID esistenti nel tuo DB se vuoi testare per davvero)
        int idUtenteTest = 1; 
        LocalDate dataTest = LocalDate.now().plusDays(1); 
        int idFasciaTest = 1; 

        try {
            System.out.println("Verifica stato utente " + idUtenteTest + " in data " + dataTest);
            boolean occupato = dao.isUtenteOccupato(idUtenteTest, dataTest, idFasciaTest);
            
            // Questo test passa se non lancia eccezioni SQL
            System.out.println("Query eseguita con successo. Risultato: " + (occupato ? "OCCUPATO" : "LIBERO"));
            
        } catch (DataAccessException e) {
            fail("Errore di connessione al DB: " + e.getMessage());
        }
    }
}