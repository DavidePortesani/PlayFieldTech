package it.unipv.ingsfw.playfieldtech.manager;

import it.unipv.ingsfw.playfieldtech.dao.UtenteDAO;
import it.unipv.ingsfw.playfieldtech.exceptions.DataAccessException;
import it.unipv.ingsfw.playfieldtech.exceptions.DatiConflittoException;
import it.unipv.ingsfw.playfieldtech.exceptions.RisorsaNonTrovataException;
import it.unipv.ingsfw.playfieldtech.factory.DaoFactory;
import it.unipv.ingsfw.playfieldtech.factory.UtenteFactory;
import it.unipv.ingsfw.playfieldtech.model.Utente;

public class UserManager {
    private final UtenteDAO utenteDAO;
    private final UtenteFactory utenteFactory;

    public UserManager(DaoFactory factory) {
        this.utenteDAO = factory.getUtenteDAO();
        this.utenteFactory = new UtenteFactory();
    }

    public Utente login(String username, String password) throws RisorsaNonTrovataException, DataAccessException {
        return utenteDAO.validaLogin(username, password);
    }

    public void registraCliente(String username, String password, String nome, String cognome) 
            throws DatiConflittoException, DataAccessException {
        
        Utente nuovoUtente = utenteFactory.creaUtente("cliente", 0, username, password, nome, cognome);
        utenteDAO.salvaUtente(nuovoUtente);
    }
}