package it.unipv.ingsfw.playfieldtech.facade;

import it.unipv.ingsfw.playfieldtech.exceptions.*;
import it.unipv.ingsfw.playfieldtech.manager.TorneoManager;
import it.unipv.ingsfw.playfieldtech.model.*;
import it.unipv.ingsfw.playfieldtech.observer.Observer;

import java.util.List;
/*
* Facade per la gestione dei tornei. Fornisce un'interfaccia semplificata per le operazioni legate ai tornei,
* delegando la logica al TorneoManager.
*/
public class TorneoFacade {
    private final TorneoManager torneoManager;

    public TorneoFacade(TorneoManager torneoManager) {
        this.torneoManager = torneoManager;
    }

    public List<Torneo> getTuttiITornei() throws DataAccessException {
        return torneoManager.getTuttiITornei();
    }

    public List<Squadra> getTutteLeSquadre() throws DataAccessException {
        return torneoManager.getTutteLeSquadre();
    }

    public void creaTorneo(String nome, List<Squadra> squadre, int idAdmin)
        throws DataAccessException, RegolaDiBusinessException {

        torneoManager.creaTorneo(nome, squadre, idAdmin);
    }

    public void generaCalendario(Torneo torneo, Amministratore admin) 
            throws SlotNonTrovatoException, OperazioneFallitaException, RegolaDiBusinessException, DataAccessException {
        
        torneoManager.generaCalendario(torneo, admin);
    }

    public List<Partita> getPartiteTorneo(int idTorneo) throws DataAccessException {
        return torneoManager.getPartiteTorneo(idTorneo);
    }

    public void inserisciRisultato(int idPartita, int golCasa, int golOspite, Amministratore admin) 
            throws OperazioneFallitaException, SlotNonTrovatoException, RegolaDiBusinessException, DataAccessException {
        try {
            torneoManager.inserisciRisultato(idPartita, golCasa, golOspite, admin);
        } catch (RisorsaNonTrovataException e) {
            throw new OperazioneFallitaException("Partita non trovata durante l'inserimento risultato.", e);
        }
    }

    public void eliminaTorneo(int idTorneo) throws DataAccessException {
        torneoManager.eliminaTorneoCompleto(idTorneo);
    }

    public List<Torneo> getTorneiDiUtente(int idUtente) throws DataAccessException {
        return torneoManager.getTorneiDiUtente(idUtente);
    }

    public void addObserver(Observer o) {
        torneoManager.addObserver(o);
    }
    
    public void removeObserver(Observer o) {
        torneoManager.removeObserver(o);
    }
}
