package it.unipv.ingsfw.playfieldtech.state;

import it.unipv.ingsfw.playfieldtech.model.Torneo;
import it.unipv.ingsfw.playfieldtech.exceptions.RegolaDiBusinessException;

// Stato del torneo quando è concluso, non permette più modifiche o inserimenti di risultati
public class StatoConcluso implements StatoTorneo {
    @Override
    public String getNomeStato() { return "CONCLUSO"; }

    @Override
    public void generaCalendario(Torneo torneo) throws RegolaDiBusinessException {
        throw new RegolaDiBusinessException("Il torneo è concluso.");
    }

    @Override
    public void inserisciRisultato(Torneo torneo) throws RegolaDiBusinessException {
        throw new RegolaDiBusinessException("Il torneo è concluso.");
    }

    @Override
    public void concludiTorneo(Torneo torneo) throws RegolaDiBusinessException {
        throw new RegolaDiBusinessException("Il torneo è già concluso.");
    }
}