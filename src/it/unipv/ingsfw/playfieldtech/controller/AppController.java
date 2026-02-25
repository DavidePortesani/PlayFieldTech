package it.unipv.ingsfw.playfieldtech.controller;

import it.unipv.ingsfw.playfieldtech.exceptions.*;
import it.unipv.ingsfw.playfieldtech.facade.*;
import it.unipv.ingsfw.playfieldtech.factory.DaoFactory;
import it.unipv.ingsfw.playfieldtech.manager.*;
import it.unipv.ingsfw.playfieldtech.model.*;
import it.unipv.ingsfw.playfieldtech.view.*;

public class AppController {
    private final MainFrame mainFrame;
    
    //Facades
    private final UserFacade userFacade;
    private final TorneoFacade torneoFacade;
    private final PrenotazioneFacade prenotazioneFacade;
    private SquadraFacade squadraFacade;

    //Managers
    private final SquadraManager squadraManager;
    private final TorneoManager torneoManager;
    private final PrenotazioneManager prenotazioneManager;
    private UserManager userManager;
    //Sub-Controllers
    private ClienteController clienteController;
    private AdminController adminController;
    
    //Stato
    private Utente utenteCorrente;
   

    public AppController(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        DaoFactory daoFactory = DaoFactory.getInstance();

        //Creiamo i Manager passando la Factory
        this.prenotazioneManager = new PrenotazioneManager(daoFactory);
        this.torneoManager = new TorneoManager(daoFactory, prenotazioneManager); 
        this.squadraManager = new SquadraManager(daoFactory, prenotazioneManager); 
        this.userManager = new UserManager(daoFactory);

        //Creiamo le Facade passando i Manager (INIEZIONE)
        this.torneoFacade = new TorneoFacade(torneoManager);
        this.prenotazioneFacade = new PrenotazioneFacade(prenotazioneManager);
        this.userFacade = new UserFacade(userManager);
        this.squadraFacade = new SquadraFacade(squadraManager);

        this.clienteController = new ClienteController(this, mainFrame, mainFrame.getClienteView());
        this.adminController = new AdminController(this, mainFrame, mainFrame.getAdminView());

        //Setup Listener
        initController();
    }

    private void initController() {
        mainFrame.getLoginView().addLoginListener(_ -> eseguiLogin());
        mainFrame.getLoginView().addRegistratiListener(_ -> mainFrame.showRegistrazioneView());
        mainFrame.getRegistrazioneView().addRegistrazioneListener(_ -> eseguiRegistrazione());
        mainFrame.getRegistrazioneView().addTornaAlLoginListener(_ -> mainFrame.showLoginView());
    }

    private void eseguiLogin() {
        LoginView loginView = mainFrame.getLoginView();
        String username = loginView.getUsername();
        String password = new String(loginView.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            mainFrame.mostraErrore("Inserire username e password.");
            return;
        }

        try {
            utenteCorrente = userFacade.login(username, password);
            
            if (utenteCorrente instanceof Cliente) {
                clienteController.onLogin();
                mainFrame.showClienteView();
            } else if (utenteCorrente instanceof Amministratore) {
                adminController.onLogin();
                mainFrame.showAdminView();
            }
        } catch (RisorsaNonTrovataException e) {
            mainFrame.mostraErrore("Credenziali non valide.");
        } catch (DataAccessException e) {
            mainFrame.mostraErrore("Errore di connessione al database.");
            e.printStackTrace();
        } finally {
            loginView.clearFields();
        }
    }

    private void eseguiRegistrazione() {
        RegistrazioneView regView = mainFrame.getRegistrazioneView();
        String nome = regView.getNome();
        String cognome = regView.getCognome();
        String username = regView.getUsername();
        String password = new String(regView.getPassword());
        String confPass = new String(regView.getConfermaPassword());

        if (nome.isEmpty() || cognome.isEmpty() || username.isEmpty() || password.isEmpty()) {
            mainFrame.mostraErrore("Tutti i campi sono obbligatori.");
            return;
        }

        //Controllo Limite Caratteri (30)
        if (nome.length() > 30 || cognome.length() > 30 || username.length() > 30 || password.length() > 30) {
            mainFrame.mostraErrore("Nessun campo può superare i 30 caratteri.");
            return;
        }

        //Solo Caratteri Alfabetici (per nome e cognome)
        //La regex "[a-zA-Z]+" significa: dall'inizio alla fine, solo lettere maiuscole o minuscole.
        if (!nome.matches("[a-zA-Z]+") || !cognome.matches("[a-zA-Z]+")) {
            mainFrame.mostraErrore("Nome e Cognome devono contenere solo lettere dell'alfabeto (niente numeri o simboli).");
            return;
        }

        if (!password.equals(confPass)) {
            mainFrame.mostraErrore("Le password non coincidono.");
            return;
        }

        try {
            userFacade.registraCliente(username, password, nome, cognome);
            mainFrame.mostraSuccesso("Registrazione completata!");
            regView.clearFields();
            mainFrame.showLoginView();
        } catch (DatiConflittoException e) {
            mainFrame.mostraErrore(e.getMessage());
        } catch (DataAccessException e) {
            mainFrame.mostraErrore("Errore tecnico durante la registrazione.");
            e.printStackTrace();
        }
    }

    public void eseguiLogout() {
        utenteCorrente = null;
        mainFrame.showLoginView();
    }

    //Metodi get
    public Utente getUtenteCorrente() { return utenteCorrente; }

    public MainFrame getMainFrame() { return mainFrame; }
    
    public UserFacade getUserFacade() { return userFacade; }
    public TorneoFacade getTorneoFacade() { return torneoFacade; }
    public PrenotazioneFacade getPrenotazioneFacade() { return prenotazioneFacade; }
    public SquadraFacade getSquadraFacade() { return squadraFacade; }
    
    
    public SquadraManager getSquadraManager() { return squadraManager; }
    public TorneoManager getTorneoManager() { return torneoManager; }
    public PrenotazioneManager getPrenotazioneManager() { return prenotazioneManager; }
    public UserManager getUserManager() { return userManager; }
    
}