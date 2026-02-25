package it.unipv.ingsfw.playfieldtech.controller;

import it.unipv.ingsfw.playfieldtech.exceptions.DataAccessException;
import it.unipv.ingsfw.playfieldtech.exceptions.RegolaDiBusinessException;
import it.unipv.ingsfw.playfieldtech.exceptions.RisorsaNonTrovataException;
import it.unipv.ingsfw.playfieldtech.facade.PrenotazioneFacade;
import it.unipv.ingsfw.playfieldtech.facade.SquadraFacade;
import it.unipv.ingsfw.playfieldtech.model.*;
import it.unipv.ingsfw.playfieldtech.view.*;

import java.util.List;

public class AdminController {

    private final AppController appController;
    private final MainFrame mainFrame;
    private final AdminView view;
    private final TorneoController torneoController;
    private final PrenotazioneFacade prenotazioneFacade; 
    private final SquadraFacade squadraFacade;

    public AdminController(AppController app, MainFrame frame, AdminView view) {
        this.appController = app;
        this.mainFrame = frame;
        this.view = view;
        
        // Recuperiamo la facade dall'AppController
        this.prenotazioneFacade = app.getPrenotazioneFacade();
        // Recuperiamo la SquadraFacade dall'AppController
        this.squadraFacade = app.getSquadraFacade();
        // Crea i controller specializzati di cui ha bisogno
        this.torneoController = new TorneoController(app); 
        
        initListeners();
    }

    private void initListeners() {
        view.addLogoutListener(_ -> appController.eseguiLogout());
        view.addAggiungiImpiantoListener(_ -> mostraDialogAggiungiImpianto());
        view.addVisualizzaPrenotazioniListener(_ -> mostraTutteLePrenotazioni());
        
        // Delega al controller specializzato
        view.addGestisciTorneiListener(_ -> torneoController.mostraGestioneTornei());
        view.addGestioneSquadreListener(_ -> mostraGestioneSquadre());
    }

    public void onLogin() {
        view.setWelcomeMessage(appController.getUtenteCorrente().getNome());
    }

    private void mostraDialogAggiungiImpianto() {
        try {
            List<FasciaOraria> tutteLeFasce = prenotazioneFacade.getTutteFasceOrarie();
            
            if (tutteLeFasce.isEmpty()) { 
                mainFrame.mostraErrore("Non ci sono fasce orarie configurate.");
                return; 
            }

            AggiungiImpiantoDialog dialog = new AggiungiImpiantoDialog(mainFrame, tutteLeFasce);
            dialog.setVisible(true);

            if (dialog.isConfermato()) {
                String nomeImpianto = dialog.getNomeImpianto();
                List<FasciaOraria> fasceSelezionate = dialog.getSelectedFasce();

                if (nomeImpianto.trim().isEmpty() || fasceSelezionate.isEmpty()) { 
                    mainFrame.mostraErrore("Nome impianto e almeno una fascia oraria sono obbligatori.");
                    return; 
                }
                
                prenotazioneFacade.aggiungiImpianto(nomeImpianto, fasceSelezionate);
                
                mainFrame.mostraSuccesso("Impianto aggiunto!");
            }
         
        } catch (RegolaDiBusinessException e) {
            // Mostra il messaggio specifico dell'errore di univocità all'utente
            mainFrame.mostraErrore(e.getMessage());
        } catch (DataAccessException e) {
            mainFrame.mostraErrore("Errore DB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void mostraTutteLePrenotazioni() {
        try {
            List<Prenotazione> tutteLePrenotazioni = prenotazioneFacade.getTutteLePrenotazioni();
            
            if (tutteLePrenotazioni.isEmpty()) {
                mainFrame.mostraErrore("Nessuna prenotazione registrata.");
            } else {
                TuttePrenotazioniDialog dialog = new TuttePrenotazioniDialog(mainFrame, tutteLePrenotazioni);
                dialog.setVisible(true);
            }
        } catch (DataAccessException e) {
            mainFrame.mostraErrore("Errore DB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void rimuoviSquadra(int idSquadra) {
        try {
            appController.getSquadraFacade().rimuoviSquadraAdmin(idSquadra);
        } catch (DataAccessException e) {
            mainFrame.mostraErrore("Errore eliminazione squadra: ");
        } catch (RisorsaNonTrovataException e) {
            mainFrame.mostraErrore("Credenziali non valide.");
        }
    }

    private void mostraGestioneSquadre() {
        try {
            List<Squadra> squadre = squadraFacade.getTutteSquadre();
            
            GestioneSquadreAdminDialog dialog = new GestioneSquadreAdminDialog(view.getFrame(), squadre, this);
            dialog.setVisible(true);
            
        } catch (DataAccessException ex) {
            mainFrame.mostraErrore("Errore nel caricamento squadre: " + ex.getMessage());
        }
    }
}