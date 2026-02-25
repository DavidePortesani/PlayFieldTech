package it.unipv.ingsfw.playfieldtech.manager;

import java.util.List;

import it.unipv.ingsfw.playfieldtech.dao.SquadraDAO;
import it.unipv.ingsfw.playfieldtech.dao.UtenteDAO;
import it.unipv.ingsfw.playfieldtech.exceptions.*;
import it.unipv.ingsfw.playfieldtech.factory.DaoFactory;
import it.unipv.ingsfw.playfieldtech.model.Cliente;
import it.unipv.ingsfw.playfieldtech.model.Squadra;
import it.unipv.ingsfw.playfieldtech.model.Utente;

// Manager che si occupa della logica di business relativa alle squadre
public class SquadraManager {

    private final SquadraDAO squadraDAO;
    private final UtenteDAO utenteDAO;
    private final PrenotazioneManager prenotazioneManager; 

    public SquadraManager(DaoFactory factory, PrenotazioneManager prenotazioneManager) {
        this.squadraDAO = factory.getSquadraDAO();
        this.utenteDAO = factory.getUtenteDAO();
        this.prenotazioneManager = prenotazioneManager;
    }

    /*
    * Crea una nuova squadra con un capitano specificato. Il capitano viene automaticamente aggiunto come membro della squadra.
    * Lancia RegolaDiBusinessException se il nome della squadra è vuoto o se esiste già una squadra con lo stesso nome.
    */
    public void creaSquadra(String nomeSquadra, Cliente capitano) throws RegolaDiBusinessException, DataAccessException {
        //
        if (nomeSquadra == null || nomeSquadra.trim().isEmpty()) {
            throw new RegolaDiBusinessException("Il nome della squadra non può essere vuoto.");
        }
        
        Squadra nuovaSquadra = new Squadra(0, nomeSquadra, capitano, capitano.getIdUtente());
        try {
            squadraDAO.salvaSquadra(nuovaSquadra);
            squadraDAO.aggiungiMembro(nuovaSquadra.getIdSquadra(), capitano.getIdUtente()); 
        } catch (DatiConflittoException e) {
            throw new RegolaDiBusinessException("Esiste già una squadra con questo nome."); 
        }
    }
    /*
    * Aggiunge un membro a una squadra esistente. Solo il capitano della squadra può aggiungere membri.
    * Lancia RegolaDiBusinessException se l'utente da aggiungere è un amministratore, se è già membro della squadra o se la squadra ha già 10 membri.
    */
    public void aggiungiMembro(Squadra s, String username, Cliente capitanoRichiedente) 
            throws RegolaDiBusinessException, DataAccessException {
        
        if (capitanoRichiedente.getIdUtente() != s.getIdCapitano()) {
            throw new PermessoNegatoException("Solo il capitano può aggiungere membri.");
        }
        
        //Controllo Limite Giocatori (max 10)
        List<Cliente> membriAttuali = squadraDAO.getMembriBySquadra(s.getIdSquadra());
        if (membriAttuali.size() >= 10) {
            throw new RegolaDiBusinessException("La squadra ha raggiunto il limite massimo di 10 giocatori.");
        }
        
        Utente u = utenteDAO.findUtenteByUsername(username); 
        
        if (!(u instanceof Cliente)) {
            throw new RegolaDiBusinessException("L'utente '" + username + "' è un amministratore, non un cliente.");
        }
        
        // Controlla se è già membro
        for(Cliente membro : membriAttuali) {
            if(membro.getIdUtente() == u.getIdUtente()) {
                throw new RegolaDiBusinessException("L'utente fa già parte della squadra.");
            }
        }

        squadraDAO.aggiungiMembro(s.getIdSquadra(), u.getIdUtente());
    }

    /*
    * Rimuove un membro da una squadra. Solo il capitano della squadra può rimuovere membri, e non è possibile rimuovere il capitano stesso.
    * Lancia PermessoNegatoException se l'utente che esegue l'operazione non è il capitano, RegolaDiBusinessException se si tenta di rimuovere il capitano o 
    * se l'utente da rimuovere non è un membro della squadra.
    */
    public void rimuoviMembro(Squadra s, Cliente membroDaRimuovere, Cliente utenteCheEsegue) 
            throws PermessoNegatoException, RegolaDiBusinessException, DataAccessException {
        
        if (utenteCheEsegue.getIdUtente() != s.getIdCapitano()) {
            throw new PermessoNegatoException();
        }
        
        if (membroDaRimuovere.getIdUtente() == s.getIdCapitano()) {
            throw new RegolaDiBusinessException("Il capitano non può essere rimosso dalla squadra.");
        }
        
        squadraDAO.rimuoviMembro(s.getIdSquadra(), membroDaRimuovere.getIdUtente());
    }

    /*
     * Rimuove una squadra amministrativamente. Lancia RisorsaNonTrovataException se la squadra non esiste.
     */
    public void rimuoviSquadraAdmin(int idSquadra) throws DataAccessException, RisorsaNonTrovataException {
        Squadra squadraDaEliminare = squadraDAO.getSquadraById(idSquadra);
        
        if (squadraDaEliminare == null) {
            throw new RisorsaNonTrovataException("Impossibile trovare la squadra con ID " + idSquadra);
        }

        //  Chiamiamo il PrenotazioneManager per liberare i campi occupati da questa squadra
        // Nota: stiamo usando il Singleton di PrenotazioneManager
        prenotazioneManager.cancellaPrenotazioniDiSquadra(squadraDaEliminare.getNomeSquadra());
        squadraDAO.eliminaSquadra(idSquadra);
    }

     public List<Squadra> getSquadreDiUtente(int idUtente) throws DataAccessException {
        return squadraDAO.getSquadreByUtente(idUtente);
    }

    public List<Cliente> getMembriSquadra(int idSquadra) throws DataAccessException {
        return squadraDAO.getMembriBySquadra(idSquadra);
    }

    public List<Squadra> getTutteSquadre() throws DataAccessException {
        return squadraDAO.getTutteLeSquadre();
    }
}