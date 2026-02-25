package it.unipv.ingsfw.playfieldtech.state;

import it.unipv.ingsfw.playfieldtech.model.Torneo;
import it.unipv.ingsfw.playfieldtech.exceptions.RegolaDiBusinessException;

// Stato del torneo quando è attivo, permette l'inserimento dei risultati e la conclusione del torneo
public class StatoAttivo implements StatoTorneo {
    @Override
    public String getNomeStato() { return "ATTIVO"; }

    @Override
    public void generaCalendario(Torneo torneo) throws RegolaDiBusinessException {
        throw new RegolaDiBusinessException("Il calendario è già stato generato.");
    }

    @Override
    public void inserisciRisultato(Torneo torneo) throws RegolaDiBusinessException {
        // Permesso. Non cambia stato finché non si chiama concludiTorneo.
    }

    @Override
    public void concludiTorneo(Torneo torneo) throws RegolaDiBusinessException {
        torneo.setStatoObj(new StatoConcluso());
    }
}