package it.unipv.ingsfw.playfieldtech.facade;

import java.util.List;

import it.unipv.ingsfw.playfieldtech.exceptions.DataAccessException;
import it.unipv.ingsfw.playfieldtech.exceptions.PermessoNegatoException;
import it.unipv.ingsfw.playfieldtech.exceptions.RegolaDiBusinessException;
import it.unipv.ingsfw.playfieldtech.exceptions.RisorsaNonTrovataException;
import it.unipv.ingsfw.playfieldtech.manager.SquadraManager;
import it.unipv.ingsfw.playfieldtech.model.Cliente;
import it.unipv.ingsfw.playfieldtech.model.Squadra;

/*
* Facade per la gestione delle squadre. Fornisce un'interfaccia semplificata per le operazioni legate alle squadre,
* delegando la logica al SquadraManager.
*/
public class SquadraFacade {

    private final SquadraManager squadraManager;

    public SquadraFacade(SquadraManager squadraManager) {
        this.squadraManager = squadraManager;
    }

    public void creaSquadra(String nomeSquadra, Cliente capitano) throws RegolaDiBusinessException, DataAccessException {
        squadraManager.creaSquadra(nomeSquadra, capitano);
    }

    public void aggiungiMembro(Squadra s, String usernameNuovoMembro, Cliente utenteCheEsegue) 
            throws RegolaDiBusinessException, DataAccessException {
        squadraManager.aggiungiMembro(s, usernameNuovoMembro, utenteCheEsegue);
    }

    public void rimuoviMembro(Squadra s, Cliente membroDaRimuovere, Cliente utenteCheEsegue) 
            throws PermessoNegatoException, RegolaDiBusinessException, DataAccessException {
        squadraManager.rimuoviMembro(s, membroDaRimuovere, utenteCheEsegue);
    }

    public void rimuoviSquadraAdmin(int idSquadra) throws DataAccessException, RisorsaNonTrovataException {
        squadraManager.rimuoviSquadraAdmin(idSquadra);
    }

    public List<Squadra> getSquadreDiUtente(int idUtente) throws DataAccessException {
        return squadraManager.getSquadreDiUtente(idUtente);
    }

    public List<Squadra> getTutteSquadre() throws DataAccessException {
        return squadraManager.getTutteSquadre();
    }

    public List<Cliente> getMembriSquadra(int idSquadra) throws DataAccessException {
        return squadraManager.getMembriSquadra(idSquadra);
    }
}