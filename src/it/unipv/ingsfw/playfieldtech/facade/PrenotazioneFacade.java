package it.unipv.ingsfw.playfieldtech.facade;

import it.unipv.ingsfw.playfieldtech.exceptions.DataAccessException;
import it.unipv.ingsfw.playfieldtech.manager.PrenotazioneManager;
import it.unipv.ingsfw.playfieldtech.model.*;
import it.unipv.ingsfw.playfieldtech.observer.Observer;
import it.unipv.ingsfw.playfieldtech.strategy.PriceCalculator;
import it.unipv.ingsfw.playfieldtech.exceptions.PermessoNegatoException;
import it.unipv.ingsfw.playfieldtech.exceptions.RegolaDiBusinessException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class PrenotazioneFacade {
    private final PrenotazioneManager prenotazioneManager;
    private final PriceCalculator priceCalculator;

    public PrenotazioneFacade(PrenotazioneManager prenotazioneManager) {        
        this.prenotazioneManager = prenotazioneManager;
        this.priceCalculator = new PriceCalculator();
    }

    public void aggiungiImpianto(String nome, List<FasciaOraria> fasce) throws RegolaDiBusinessException, DataAccessException {
        // Delega tutta la responsabilità al Manager
        prenotazioneManager.aggiungiImpianto(nome, fasce);
    }

    // Metodi per il Calendario 
    public List<ImpiantoSportivo> getTuttiImpianti() throws DataAccessException {
        return prenotazioneManager.getTuttiImpianti();
    }

    public List<FasciaOraria> getTutteFasceOrarie() throws DataAccessException {
        return prenotazioneManager.getTutteFasceOrarie();
    }

    public List<Prenotazione> getPrenotazioniGiorno(LocalDate data) throws DataAccessException {
        return prenotazioneManager.getPrenotazioniGiorno(data);
    }

    public Map<Integer, List<Integer>> getMappaDisponibilita() throws DataAccessException {
        return prenotazioneManager.getMappaDisponibilita();
    }
    
    // Metodi per la Prenotazione 
    
    public double calcolaPrezzo(Prenotazione p) {
        return priceCalculator.calcolaPrezzoFinale(p);
    }

    public void confermaPrenotazione(Prenotazione p, Squadra casa, Squadra ospite, Utente creatore) 
        throws DataAccessException, PermessoNegatoException, RegolaDiBusinessException { 
        
        prenotazioneManager.confermaPrenotazione(p, casa, ospite, creatore);
    }

    public List<Prenotazione> getStoricoUtente(int idUtente) throws DataAccessException {
        return prenotazioneManager.getStoricoUtente(idUtente);
    }
    
    public List<Prenotazione> getTutteLePrenotazioni() throws DataAccessException {
        return prenotazioneManager.getTutteLePrenotazioni();
    }

    public void addObserver(Observer o) { 
        prenotazioneManager.addObserver(o);
    }
    
}