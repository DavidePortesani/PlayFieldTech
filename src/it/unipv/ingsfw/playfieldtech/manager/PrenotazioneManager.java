package it.unipv.ingsfw.playfieldtech.manager;

import it.unipv.ingsfw.playfieldtech.dao.FasciaOrariaDAO;
import it.unipv.ingsfw.playfieldtech.dao.ImpiantiFasceDAO;
import it.unipv.ingsfw.playfieldtech.dao.ImpiantoSportivoDAO;
import it.unipv.ingsfw.playfieldtech.dao.PrenotazioneDAO;
import it.unipv.ingsfw.playfieldtech.dao.SquadraDAO;
import it.unipv.ingsfw.playfieldtech.exceptions.DataAccessException;
import it.unipv.ingsfw.playfieldtech.exceptions.PermessoNegatoException;
import it.unipv.ingsfw.playfieldtech.exceptions.RegolaDiBusinessException;
import it.unipv.ingsfw.playfieldtech.factory.DaoFactory;
import it.unipv.ingsfw.playfieldtech.model.*;
import it.unipv.ingsfw.playfieldtech.observer.Observer;
import it.unipv.ingsfw.playfieldtech.observer.Subject;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PrenotazioneManager implements Subject {

    private final List<Observer> observers;
    private final PrenotazioneDAO prenotazioneDAO;
    private final SquadraDAO squadraDAO;
    private final ImpiantoSportivoDAO impiantoDAO;
    private final ImpiantiFasceDAO impiantiFasceDAO;
    private final FasciaOrariaDAO fasciaOrariaDAO;

    public PrenotazioneManager(DaoFactory factory) {
        this.observers = new ArrayList<>();
        this.prenotazioneDAO = factory.getPrenotazioneDAO();
        this.squadraDAO = factory.getSquadraDAO(); 
        this.impiantoDAO = factory.getImpiantoDAO(); 
        this.impiantiFasceDAO = factory.getImpiantiFasceDAO(); 
        this.fasciaOrariaDAO = factory.getFasciaOrariaDAO(); 
    }

    @Override
    public void addObserver(Observer o) { observers.add(o); }
    @Override
    public void removeObserver(Observer o) { observers.remove(o); }
    @Override
    public void notifyObservers() {
        for (Observer observer : observers) {
            observer.update();
        }
    }

    public void confermaPrenotazione(Prenotazione p, Squadra casa, Squadra ospite, Utente creatore) 
        throws DataAccessException, PermessoNegatoException, RegolaDiBusinessException { 
    
        // 1. Controllo membership per possibili implementazioni future
        if (!(creatore instanceof Amministratore)) {
            boolean inCasa = squadraDAO.isUtenteMembro(casa.getIdSquadra(), creatore.getIdUtente());
            boolean inOspite = squadraDAO.isUtenteMembro(ospite.getIdSquadra(), creatore.getIdUtente());

            if (!inCasa && !inOspite) {
                throw new PermessoNegatoException("Non puoi prenotare: devi far parte di una delle due squadre.");
            }
        }

        // Recupero membri
        List<Cliente> membriCasa = squadraDAO.getMembriBySquadra(casa.getIdSquadra());
        List<Cliente> membriOspite = squadraDAO.getMembriBySquadra(ospite.getIdSquadra());
        
        List<Cliente> tuttiIGiocatori = new ArrayList<>();
        tuttiIGiocatori.addAll(membriCasa);
        tuttiIGiocatori.addAll(membriOspite);
        
        if (casa.getCapitano() != null && !tuttiIGiocatori.contains(casa.getCapitano())) tuttiIGiocatori.add(casa.getCapitano());
        if (ospite.getCapitano() != null && !tuttiIGiocatori.contains(ospite.getCapitano())) tuttiIGiocatori.add(ospite.getCapitano());

        // 3. Controllo sovrapposizioni
        for (Cliente c : tuttiIGiocatori) {
            boolean occupato = prenotazioneDAO.isUtenteOccupato(c.getIdUtente(), p.getData(), p.getFasciaOraria().getIdFascia());
            if (occupato) {
                throw new RegolaDiBusinessException("Impossibile prenotare: almeno un giocatore ha già una partita in questa data e ora!");
            }
        }

        // Preparazione oggetto prenotazione
        p.setNomeSquadraA(casa.getNomeSquadra());
        p.setNomeSquadraB(ospite.getNomeSquadra());
        p.setPagamentoConfermato(true);
        p.setCreatore(creatore);
        p.setGiocatori(tuttiIGiocatori);

        // Salvataggio e notifica
        prenotazioneDAO.salvaPrenotazione(p);
        notifyObservers();
    }

    public void aggiungiImpianto(String nome, List<FasciaOraria> fasce) throws RegolaDiBusinessException, DataAccessException {
        // Regola di business: univocità del nome
        List<ImpiantoSportivo> esistenti = impiantoDAO.getTuttiImpianti();
        for (ImpiantoSportivo i : esistenti) {
            if (i.getNome().equalsIgnoreCase(nome.trim())) {
                throw new RegolaDiBusinessException("Impossibile aggiungere l'impianto: il nome '" + nome + "' è già in uso.");
            }
        }

        // Creazione e salvataggio sul DB
        ImpiantoSportivo nuovo = new ImpiantoSportivo(0, nome, "Calcetto");
        impiantoDAO.salvaImpiantoConFasce(nuovo, fasce);
    }

    public void cancellaPrenotazioniDiSquadra(String nomeSquadra) throws DataAccessException {
        prenotazioneDAO.eliminaPrenotazioniPerSquadra(nomeSquadra);
        notifyObservers();
    }

   

    public List<ImpiantoSportivo> getTuttiImpianti() throws DataAccessException {
        return impiantoDAO.getTuttiImpianti();
    }

    public List<FasciaOraria> getTutteFasceOrarie() throws DataAccessException {
        return fasciaOrariaDAO.getTutteFasceOrarie();
    }

    public List<Prenotazione> getPrenotazioniGiorno(LocalDate data) throws DataAccessException {
        return prenotazioneDAO.getPrenotazioniByDate(data);
    }

    public Map<Integer, List<Integer>> getMappaDisponibilita() throws DataAccessException {
        return impiantiFasceDAO.getMappaDisponibilita();
    }

    public List<Prenotazione> getStoricoUtente(int idUtente) throws DataAccessException {
        return prenotazioneDAO.getTutteLePrenotazioniPerUtente(idUtente);
    }
    
    public List<Prenotazione> getTutteLePrenotazioni() throws DataAccessException {
        return prenotazioneDAO.getTutteLePrenotazioni();
    }
}