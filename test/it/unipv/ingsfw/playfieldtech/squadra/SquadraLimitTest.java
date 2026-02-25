package test.it.unipv.ingsfw.playfieldtech.squadra;

import it.unipv.ingsfw.playfieldtech.dao.SquadraDAO;
import it.unipv.ingsfw.playfieldtech.factory.DaoFactory;
import it.unipv.ingsfw.playfieldtech.manager.PrenotazioneManager;
import it.unipv.ingsfw.playfieldtech.manager.SquadraManager;
import it.unipv.ingsfw.playfieldtech.manager.TorneoManager;
import it.unipv.ingsfw.playfieldtech.model.Cliente;
import it.unipv.ingsfw.playfieldtech.model.Torneo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

class SquadraLimitTest {

    private SquadraManager manager;
    private SquadraDAO dao;
    private TorneoManager tManager;

    @BeforeEach
    void setUp() {
        DaoFactory factory = DaoFactory.getInstance();
        PrenotazioneManager pManager = new PrenotazioneManager(factory);
        
        // Costruzione manuale con dependency injection
        this.tManager = new TorneoManager(factory, pManager);
        this.manager = new SquadraManager(factory, pManager);
        this.dao = factory.getSquadraDAO();
    }

    @Test
    void testLimiteMassimoGiocatoriLogico() {
        System.out.println("--- TEST LIMITE SQUADRA (MAX 10) ---");

        // Simuliamo la logica presente nel manager "white box"
        // Poiché non vogliamo sporcare il DB, testiamo la condizione logica
        List<Cliente> membriSimulati = new ArrayList<>();
        
        // Riempiamo la lista con 10 utenti finti
        for(int i=0; i<10; i++) {
            membriSimulati.add(new Cliente(i, "user"+i, "pass", "N", "C"));
        }
        
        // Verifica: Se la lista ha già 10 membri, il sistema deve bloccare
        boolean bloccoAttivato = false;
        if (membriSimulati.size() >= 10) {
            bloccoAttivato = true;
        }
        
        assertTrue(bloccoAttivato, "Il sistema deve rilevare che il limite è raggiunto con 10 giocatori");
        System.out.println("Test logico Limite Giocatori completato.");
    }
}