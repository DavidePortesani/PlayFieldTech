package test.it.unipv.ingsfw.playfieldtech.torneo;

import it.unipv.ingsfw.playfieldtech.exceptions.RegolaDiBusinessException;
import it.unipv.ingsfw.playfieldtech.factory.DaoFactory;
import it.unipv.ingsfw.playfieldtech.manager.PrenotazioneManager;
import it.unipv.ingsfw.playfieldtech.manager.TorneoManager;
import it.unipv.ingsfw.playfieldtech.model.Amministratore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TorneoManagerTest {

    private TorneoManager manager;

    @BeforeEach
    void setUp() {
        // SETUP MANUALE DELLE DIPENDENZE
        // 1. Otteniamo la factory
        DaoFactory factory = DaoFactory.getInstance();
        
        // 2. Creiamo le dipendenze necessarie (PrenotazioneManager)
        PrenotazioneManager pManager = new PrenotazioneManager(factory);
        
        // 3. Creiamo il TorneoManager iniettando tutto
        this.manager = new TorneoManager(factory, pManager);
    }

    @Test
    void testLimiteGolMassimi() {
        System.out.println("--- TEST LIMITE GOL (MAX 60) ---");
        
        Amministratore admin = new Amministratore(1, "admin", "admin", "Admin", "User");
        int idPartita = 1; // Assumi che esista o che il controllo avvenga prima del DB

        // Caso 1: Gol Casa > 60 -> Deve fallire
        Exception exceptionCasa = assertThrows(RegolaDiBusinessException.class, () -> {
            manager.inserisciRisultato(idPartita, 61, 2, admin);
        });
        assertEquals("Il numero di gol non può superare il limite di 60.", exceptionCasa.getMessage());
        
        // Caso 2: Gol Ospite > 60 -> Deve fallire
        Exception exceptionOspite = assertThrows(RegolaDiBusinessException.class, () -> {
            manager.inserisciRisultato(idPartita, 2, 80, admin);
        });
        assertEquals("Il numero di gol non può superare il limite di 60.", exceptionOspite.getMessage());

        System.out.println("Test Limite Gol Superato con successo.");
    }
}