package it.unipv.ingsfw.playfieldtech.state;

import it.unipv.ingsfw.playfieldtech.model.Torneo;
import it.unipv.ingsfw.playfieldtech.exceptions.RegolaDiBusinessException;

// Stato del torneo quando è in bozza, permette solo la generazione del calendario
public class StatoBozza implements StatoTorneo {
    @Override
    public String getNomeStato() { return "BOZZA"; }

    @Override
    public void generaCalendario(Torneo torneo) throws RegolaDiBusinessException {
        // Operazione permessa: Si passa allo stato Attivo!
        torneo.setStatoObj(new StatoAttivo());
    }

    @Override
    public void inserisciRisultato(Torneo torneo) throws RegolaDiBusinessException {
        throw new RegolaDiBusinessException("Impossibile inserire risultati: il calendario non è ancora stato generato.");
    }

    @Override
    public void concludiTorneo(Torneo torneo) throws RegolaDiBusinessException {
        throw new RegolaDiBusinessException("Un torneo in bozza non può essere concluso.");
    }
}