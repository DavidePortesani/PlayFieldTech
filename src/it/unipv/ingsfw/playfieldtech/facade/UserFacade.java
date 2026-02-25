package it.unipv.ingsfw.playfieldtech.facade;

import it.unipv.ingsfw.playfieldtech.exceptions.DataAccessException;
import it.unipv.ingsfw.playfieldtech.exceptions.DatiConflittoException;
import it.unipv.ingsfw.playfieldtech.exceptions.RisorsaNonTrovataException;
import it.unipv.ingsfw.playfieldtech.manager.UserManager;
import it.unipv.ingsfw.playfieldtech.model.Utente;

public class UserFacade {
    private final UserManager userManager;

    public UserFacade(UserManager userManager) {
        this.userManager = userManager;
    }

    /**
     * Tenta il login e restituisce l'utente se le credenziali sono corrette.
     */
    public Utente login(String username, String password) throws RisorsaNonTrovataException, DataAccessException {
        return userManager.login(username, password);
    }

    /**
     * Gestisce la registrazione di un nuovo cliente.
     */
    public void registraCliente(String username, String password, String nome, String cognome) 
            throws DatiConflittoException, DataAccessException {
        userManager.registraCliente(username, password, nome, cognome);
    }    
}