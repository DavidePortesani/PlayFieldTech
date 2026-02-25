package it.unipv.ingsfw.playfieldtech.state;

import it.unipv.ingsfw.playfieldtech.model.Torneo;
import it.unipv.ingsfw.playfieldtech.exceptions.RegolaDiBusinessException;

// Interfaccia che definisce i metodi per gestire gli stati di un torneo
public interface StatoTorneo {
    
    String getNomeStato();
    
    // Azioni permesse/negate in base allo stato
    void generaCalendario(Torneo torneo) throws RegolaDiBusinessException;
    void inserisciRisultato(Torneo torneo) throws RegolaDiBusinessException;
    void concludiTorneo(Torneo torneo) throws RegolaDiBusinessException;
}
